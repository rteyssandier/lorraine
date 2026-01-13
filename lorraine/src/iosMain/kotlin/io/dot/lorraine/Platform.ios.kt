@file:OptIn(ExperimentalUuidApi::class)

package io.dot.lorraine

import io.dot.lorraine.constraint.BatteryNotLowCheck
import io.dot.lorraine.constraint.ConnectivityCheck
import io.dot.lorraine.constraint.ConstraintCheck
import io.dot.lorraine.constraint.match
import io.dot.lorraine.db.entity.WorkerEntity
import io.dot.lorraine.db.entity.createWorkerEntity
import io.dot.lorraine.db.entity.toDomain
import io.dot.lorraine.db.entity.toInfo
import io.dot.lorraine.dsl.LorraineOperation
import io.dot.lorraine.dsl.LorraineRequest
import io.dot.lorraine.models.ExistingLorrainePolicy
import io.dot.lorraine.models.LorraineApplication
import io.dot.lorraine.models.LorraineInfo
import io.dot.lorraine.logger.DefaultLogger
import io.dot.lorraine.work.LorraineWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import platform.Foundation.NSOperation
import platform.Foundation.NSOperationQueue
import platform.Foundation.operations
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class IOSPlatform(
    private val application: LorraineApplication
) : Platform {

    override val name: String = "ios"

    private val dao = application.database.workerDao()
    private val queues: MutableMap<String, NSOperationQueue> = mutableMapOf()
    private val scope = application.scope

    private val logger = application.logger ?: DefaultLogger

    val constraints = listOf<ConstraintCheck>(
        ConnectivityCheck(
            scope = scope,
            onChange = ::constraintChanged
        ),
        BatteryNotLowCheck(
            scope = scope,
            onChange = ::constraintChanged,
            logger = logger
        )
    )

    init {
        application.scope.launch {
            dao.getWorkers()
                .groupBy(WorkerEntity::queueId)
                .forEach { (queueId, workers) ->
                    val nsOperation = NSOperationQueue()
                    var previous: NSOperation? = null

                    workers.map {
                        LorraineWorker(
                            workerUuid = Uuid.parse(it.uuid),
                            application = application,
                            platform = this@IOSPlatform
                        )
                    }
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

        dao.insert(worker)

        queue.addOperation(
            LorraineWorker(
                workerUuid = uuid,
                application = application,
                platform = this
            )
        )
        queues[worker.queueId] = queue

        queue.suspended = !constraints
            .match(worker.constraints.toDomain())
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

        workers.map {
            LorraineWorker(
                workerUuid = Uuid.parse(it.uuid),
                application = application,
                platform = this
            )
        }
            .forEach { worker ->
                previous?.let { previous -> worker.addDependency(previous) }
                previous = worker
                queue.addOperation(worker)
            }

        dao.insert(workers)

        queue.suspended = constraints
            .match(workers.first().constraints.toDomain())
    }

    internal fun suspend(uniqueId: String, suspended: Boolean) {
        val queue = queues[uniqueId] ?: return

        queue.suspended = suspended
    }

    internal fun constraintChanged() {
        scope.launch {
            val workers = dao.getWorkers()

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

                    if (constraints.match(worker.constraints.toDomain())) {
                        suspend(worker.queueId, false)
                    }
                }
        }
    }

    override suspend fun cancelWorkById(uuid: Uuid) {
        val worker = dao.getWorker(uuid.toHexString()) ?: return
        val lorraineWorker = queues[worker.queueId]?.operations
            .orEmpty()
            .filterIsInstance<LorraineWorker>()
            .find { it.workerUuid == uuid }
            ?: return

        lorraineWorker.cancel()
        dao.update(worker.copy(state = LorraineInfo.State.CANCELLED))
    }

    override suspend fun cancelUniqueWork(queueId: String) {
        val queue = queues[queueId] ?: return

        queue.operations
            .filterIsInstance<LorraineWorker>()
            .forEach { cancelWorkById(it.workerUuid) }
    }


    override suspend fun cancelAllWorkByTag(tag: String) {
        queues.flatMap { (_, operation) ->
            operation.operations
                .filterIsInstance<LorraineWorker>()
                .filter {
                    dao.getWorker(it.workerUuid.toString())
                        ?.tags
                        .orEmpty()
                        .contains(tag)
                }
        }
            .forEach { cancelWorkById(it.workerUuid) }
    }

    override suspend fun cancelAllWork() {
        queues.forEach { cancelUniqueWork(it.key) }
    }

    override suspend fun pruneWork() {
        dao.delete(
            dao.getWorkers()
                .filter { it.state.isFinished }
        )
    }

    override fun listenLorrainesInfo(): Flow<List<LorraineInfo>> = dao.getWorkersAsFlow()
        .map { list -> list.map { it.toInfo() } }

    private fun createQueue(uniqueId: String): NSOperationQueue {
        return NSOperationQueue().apply {
            setName(uniqueId)
            setMaxConcurrentOperationCount(1)
            setSuspended(true)
        }
    }
}