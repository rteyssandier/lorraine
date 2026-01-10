@file:OptIn(ExperimentalUuidApi::class)

package io.dot.lorraine.work

import io.dot.lorraine.IOSPlatform
import io.dot.lorraine.constraint.match
import io.dot.lorraine.db.entity.toDomain
import io.dot.lorraine.error.LorraineWorkCancellation
import io.dot.lorraine.models.LorraineApplication
import io.dot.lorraine.models.LorraineInfo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSBlockOperation
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class LorraineWorker(
    val workerUuid: Uuid,
    private val application: LorraineApplication,
    private val platform: IOSPlatform
) : NSBlockOperation() {

    private var workJob: Deferred<LorraineResult>? = null

    override fun isAsynchronous(): Boolean = true

    override fun main() = runBlocking {
        val dao = application.database.workerDao()
        val workerData = dao.getWorker(uuidString = workerUuid.toString()) ?: error("WorkLorraine not found")
        val identifier = requireNotNull(workerData.identifier) { "Identifier not found" }
        val workerDefinition = requireNotNull(application.definitions[identifier]) {
            "Worker definition not found"
        }

        // TODO Check dependencies
        if (!platform.constraints.match(workerData.constraints.toDomain())) {
            dao.update(workerData.copy(state = LorraineInfo.State.BLOCKED))
            return@runBlocking
        }

        val worker = workerDefinition.invoke()

        dao.update(workerData.copy(state = LorraineInfo.State.RUNNING))
        workJob = async { worker.doWork(workerData.inputData) }

        val result = workJob?.await() ?: return@runBlocking
        val state = when (result) {
            is LorraineResult.Failure -> {
                LorraineInfo.State.FAILED
            }

            is LorraineResult.Retry -> {
                // TODO Re-enqueue
                LorraineInfo.State.FAILED
            }

            is LorraineResult.Success -> {
                // TODO Delete worker if not in operation
                // TODO Delete all worker in operation, if all finish
                LorraineInfo.State.SUCCEEDED
            }
        }

        dao.update(
            workerData.copy(
                state = state,
                outputData = result.outputData
            )
        )
    }

    override fun cancel() {
        workJob?.cancel(LorraineWorkCancellation())
        super.cancel()
        application.scope.launch {
            val dao = application.database.workerDao()
            val workerData = dao.getWorker(uuidString = workerUuid.toString()) ?: return@launch

            dao.update(workerData.copy(state = LorraineInfo.State.CANCELLED))
        }
    }

}