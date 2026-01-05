@file:OptIn(ExperimentalUuidApi::class)

package io.dot.lorraine

import io.dot.lorraine.constraint.match
import io.dot.lorraine.db.entity.WorkerEntity
import io.dot.lorraine.db.entity.createWorkerEntity
import io.dot.lorraine.db.entity.toDomain
import io.dot.lorraine.db.createDatabaseBuilder
import io.dot.lorraine.db.dao.WorkerDao
import io.dot.lorraine.dsl.LorraineOperation
import io.dot.lorraine.dsl.LorraineRequest
import io.dot.lorraine.models.ExistingLorrainePolicy
import io.dot.lorraine.models.LorraineInfo
import io.dot.lorraine.work.LorraineWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import platform.Foundation.NSOperation
import platform.Foundation.NSOperationQueue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class IOSPlatform(
    private val workerDao: WorkerDao,
    coroutineScope: CoroutineScope
) : Platform {
    override val name: String = "ios"

    private val queues: MutableMap<String, NSOperationQueue> = mutableMapOf()
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        coroutineScope.launch {
            workerDao.getWorkers()
                .groupBy(WorkerEntity::queueId)
                .forEach { (queueId, workers) ->
                    val nsOperation = NSOperationQueue()
                    var previous: NSOperation? = null

                    workers.map { LorraineWorker(Uuid.parse(it.uuid)) }
                        .forEach { worker ->
                            previous?.let { previous -> worker.addDependency(previous) }
                            previous = worker
                            nsOperation.addOperation(worker)
                        }

                    queues[queueId] = nsOperation
                }

            constraintChanged()
        }
    }

    override suspend fun enqueue(
        queueId: String,
        type: ExistingLorrainePolicy,
        lorraineRequest: LorraineRequest
    ) {
        val queue = queues.getOrElse(queueId) { createQueue(queueId) }
        val uuid = Uuid.random()
        val worker = createWorkerEntity(
            uuid = uuid,
            queueId = queueId,
            request = lorraineRequest
        )

        workerDao.insert(worker)

        queue.addOperation(LorraineWorker(uuid))
        queues[worker.queueId] = queue

//        queue.suspended = !Lorraine.constraintChecks
//            .match(worker.constraints.toDomain())
    }

    override suspend fun enqueue(
        queueId: String,
        operation: LorraineOperation
    ) {
        requireNotNull(operation.operations.firstOrNull()) {
            "Operations shoud not be empty"
        }
        val queue = queues.getOrElse(queueId) { createQueue(queueId) }
        var previous: NSOperation? = null

        val workers = operation.operations
            .map { operation ->
                val uuid = Uuid.random()

                createWorkerEntity(
                    uuid = uuid,
                    queueId = queueId,
                    request = operation.request
                )
            }

        workers.map { LorraineWorker(Uuid.parse(it.uuid)) }
            .forEach { worker ->
                previous?.let { previous -> worker.addDependency(previous) }
                previous = worker
                queue.addOperation(worker)
            }

        workerDao.insert(workers)

//        queue.suspended = !Lorraine.constraintChecks
//            .match(workers.first().constraints.toDomain())
    }

    internal fun suspend(uniqueId: String, suspended: Boolean) {
        val queue = queues[uniqueId] ?: return

        queue.suspended = suspended
    }

    internal fun constraintChanged() {
        scope.launch {
            val workers = workerDao.getWorkers()

            workers.filter {
                when (it.state) {
                    LorraineInfo.State.BLOCKED,
                    LorraineInfo.State.ENQUEUED -> true

                    else -> false
                }
            }
                .forEach { worker ->
                    if (!worker.workerDependencies.all { id ->
                            workers.find { it.uuid == id }?.let {
                                it.state == LorraineInfo.State.SUCCEEDED
                            } != false
                        }) {
                        return@forEach
                    }

//                    if (Lorraine.constraintChecks
//                            .match(worker.constraints.toDomain())
//                    ) {
                    suspend(worker.queueId, false)
//                    }
                }
        }
    }

    override suspend fun cancelWorkById(uuid: Uuid) {
        workerDao.getWorker(uuid.toHexString())
        // TODO("Not yet implemented")
    }

    override suspend fun cancelUniqueWork(queueId: String) {
        // TODO("Not yet implemented")
    }

    override suspend fun cancelAllWorkByTag(tag: String) {
        // TODO("Not yet implemented")
    }

    override suspend fun cancelAllWork() {
        queues.forEach { it.value.cancelAllOperations() }
        queues.clear()
    }

    override suspend fun pruneWork() {
        // TODO("Not yet implemented")
    }

    override fun listenLorrainesInfo(): Flow<List<LorraineInfo>> {
        TODO("Not yet implemented")
    }

    private fun createQueue(uniqueId: String): NSOperationQueue {
        return NSOperationQueue().apply {
            setName(uniqueId)
            setMaxConcurrentOperationCount(1)
            setSuspended(true)
        }
    }
}