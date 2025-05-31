@file:OptIn(ExperimentalUuidApi::class)

package io.dot.lorraine

import android.annotation.SuppressLint
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.await
import androidx.work.workDataOf
import io.dot.lorraine.db.entity.WorkerEntity
import io.dot.lorraine.db.entity.toEntity
import io.dot.lorraine.db.entity.toInfo
import io.dot.lorraine.dsl.LorraineOperation
import io.dot.lorraine.dsl.LorraineRequest
import io.dot.lorraine.mapping.asLorraineData
import io.dot.lorraine.mapping.asLorraineInfoState
import io.dot.lorraine.mapping.asWorkManagerConstraints
import io.dot.lorraine.mapping.asWorkManagerData
import io.dot.lorraine.mapping.asWorkManagerExistingPolicy
import io.dot.lorraine.mapping.asWorkManagerPolicy
import io.dot.lorraine.models.ExistingLorrainePolicy
import io.dot.lorraine.models.LorraineApplication
import io.dot.lorraine.models.LorraineInfo
import io.dot.lorraine.models.workerDao
import io.dot.lorraine.work.LorraineWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.toJavaDuration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class AndroidPlatform(
    private val workManager: WorkManager,
    private val application: LorraineApplication
) : Platform {

    override val name: String = "android"

    init {
        application.scope.launch {
            workManager.getWorkInfosFlow(WorkQuery.fromStates(WorkInfo.State.entries))
                .collect { infos ->
                    infos.forEach { info ->
                        val uuid = info.id.toKotlinUuid()
                        val dao = application.workerDao
                        val worker = dao.getWorker(uuidString = uuid.toHexString())
                            ?: return@forEach

                        dao.upsert(
                            worker.copy(
                                outputData = info.outputData.asLorraineData(),
                                state = info.state.asLorraineInfoState()
                            )
                        )
                    }
                }
        }
    }

    override suspend fun enqueue(
        queueId: String,
        type: ExistingLorrainePolicy,
        lorraineRequest: LorraineRequest
    ) {
        val workManagerWorker = lorraineRequest.toWorkManagerWorker()

        application.workerDao
            .insert(
                WorkerEntity(
                    uuid = workManagerWorker.id.toKotlinUuid().toString(),
                    queueId = queueId,
                    identifier = lorraineRequest.identifier,
                    state = LorraineInfo.State.ENQUEUED,
                    tags = lorraineRequest.tags,
                    inputData = lorraineRequest.inputData,
                    outputData = null,
                    workerDependencies = emptySet(),
                    constraints = lorraineRequest.constraints.toEntity()
                )
            )

        workManager.enqueueUniqueWork(
            uniqueWorkName = queueId,
            existingWorkPolicy = type.asWorkManagerExistingPolicy(),
            request = workManagerWorker
        )
    }

    @SuppressLint("EnqueueWork")
    override suspend fun enqueue(
        queueId: String,
        operation: LorraineOperation
    ) {
        val firstOperation = operation.operations
            .first()
        val firstWorkManagerWorker = firstOperation.request
            .toWorkManagerWorker()
        var workOperation = workManager.beginUniqueWork(
            uniqueWorkName = queueId,
            existingWorkPolicy = operation.existingPolicy.asWorkManagerExistingPolicy(),
            request = firstWorkManagerWorker
        )

        workOperation = operation.operations
            .drop(1)
            .fold(workOperation) { currentWorkOperation, operation ->
                val workManagerWorker = operation.request
                    .toWorkManagerWorker()

                application.workerDao
                    .insert(
                        WorkerEntity(
                            uuid = workManagerWorker.id.toKotlinUuid().toString(),
                            queueId = queueId,
                            identifier = operation.request.identifier,
                            state = LorraineInfo.State.ENQUEUED,
                            tags = operation.request.tags,
                            inputData = operation.request.inputData,
                            outputData = null,
                            workerDependencies = emptySet(),
                            constraints = operation.request.constraints.toEntity()
                        )
                    )

                currentWorkOperation.then(workManagerWorker)
            }

        application.workerDao
            .insert(
                WorkerEntity(
                    uuid = firstWorkManagerWorker.id.toKotlinUuid().toString(),
                    queueId = queueId,
                    identifier = firstOperation.request.identifier,
                    state = LorraineInfo.State.ENQUEUED,
                    tags = firstOperation.request.tags,
                    inputData = firstOperation.request.inputData,
                    outputData = null,
                    workerDependencies = emptySet(),
                    constraints = firstOperation.request.constraints.toEntity()
                )
            )

        workOperation.enqueue()
    }

    override suspend fun cancelWorkById(uuid: Uuid) {
        workManager.cancelWorkById(uuid.toJavaUuid()).await()
    }

    override suspend fun cancelUniqueWork(queueId: String) {
        workManager.cancelUniqueWork(queueId).await()
    }

    override suspend fun cancelAllWorkByTag(tag: String) {
        workManager.cancelAllWorkByTag(tag).await()
    }

    override suspend fun cancelAllWork() {
        workManager.cancelAllWork().await()
        application.workerDao
            .delete(application.workerDao.getWorkers())
    }

    override suspend fun pruneWork() {
        workManager.pruneWork().await()
    }

    override fun listenLorrainesInfo(): Flow<List<LorraineInfo>> {
        return application.workerDao
            .getWorkersAsFlow()
            .map { list -> list.map { it.toInfo() } }
    }

    private fun LorraineRequest.toWorkManagerWorker(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<LorraineWorker>()
            .setInputData(inputData?.asWorkManagerData() ?: workDataOf())
            .setConstraints(constraints.asWorkManagerConstraints())
            .apply {
                if (backOffPolicy != null) {
                    setBackoffCriteria(
                        backoffPolicy = backOffPolicy.policy.asWorkManagerPolicy(),
                        duration = backOffPolicy.duration.toJavaDuration()
                    )
                }
            }
            .build()
    }

    companion object {

        internal var application: LorraineApplication? = null

    }

}