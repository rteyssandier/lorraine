import io.dot.lorraine.constraint.BatteryNotLowCheck
import io.dot.lorraine.constraint.ChargingCheck
import io.dot.lorraine.constraint.ConnectivityCheck
import io.dot.lorraine.constraint.ConstraintCheck
import io.dot.lorraine.constraint.match
import io.dot.lorraine.db.entity.createWorkerEntity
import io.dot.lorraine.db.entity.toDomain
import io.dot.lorraine.db.entity.toInfo
import io.dot.lorraine.dsl.LorraineOperation
import io.dot.lorraine.dsl.LorraineRequest
import io.dot.lorraine.models.ExistingLorrainePolicy
import io.dot.lorraine.models.LorraineApplication
import io.dot.lorraine.models.LorraineInfo
import io.dot.lorraine.work.LorraineWorker
import kotlin.uuid.Uuid
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.BackgroundTasks.BGProcessingTask
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTaskScheduler

internal const val BG_TASK_IDENTIFIER = "io.dot.lorraine.work"

internal class IOSPlatform(private val application: LorraineApplication) : Platform {

    override val name: String = "ios"

    private val dao = application.database.workerDao()
    private val scope = application.scope
    private val logger = application.logger

    val constraints = listOf<ConstraintCheck>(
        ConnectivityCheck(
            scope = scope,
            onChange = ::constraintChanged,
            logger = logger
        ),
        BatteryNotLowCheck(
            scope = scope,
            onChange = ::constraintChanged,
            logger = logger
        ),
        ChargingCheck(
            scope = scope,
            onChange = ::constraintChanged,
            logger = logger
        )
    )
    private val mutex = Mutex()
    private var processingJob: Job? = null

    val constraints =
            listOf<ConstraintCheck>(
                    ConnectivityCheck(
                            scope = scope,
                            onChange = ::constraintChanged,
                            logger = logger
                    ),
                    BatteryNotLowCheck(
                            scope = scope,
                            onChange = ::constraintChanged,
                            logger = logger
                    )
            )

    init {
        scope.launch { processQueue() }
    }

    override fun registerTasks() {
        val registered =
                BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
                        identifier = BG_TASK_IDENTIFIER,
                        usingQueue = null
                ) { task ->
                    if (task is BGProcessingTask) {
                        handleBackgroundTask(task)
                    }
                }
        logger.info("BGTaskScheduler registered: $registered")
    }

    private fun handleBackgroundTask(task: BGProcessingTask) {
        scheduleBackgroundTask() // Reschedule for next time

        task.expirationHandler = { processingJob?.cancel() }

        processingJob =
                scope.launch {
                    processQueue()
                    task.setTaskCompletedWithSuccess(true)
                }
    }

    private fun scheduleBackgroundTask() {
        val request = BGProcessingTaskRequest(BG_TASK_IDENTIFIER)
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false

        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
        } catch (e: Exception) {
            logger.error("Failed to submit BGTask: ${e.message}")
        }
    }

    private suspend fun processQueue() =
            mutex.withLock {
                val workers =
                        dao.getWorkers()
                                .filter {
                                    it.state == LorraineInfo.State.ENQUEUED ||
                                            it.state == LorraineInfo.State.BLOCKED
                                }
                                .sortedBy {
                                    it.id
                                } // Basic FIFO for now, should respect dependencies

                for (workerEntity in workers) {
                    // Check dependencies
                    val dependenciesMet =
                            workerEntity.workerDependencies.all { depId ->
                                dao.getWorker(depId)?.state == LorraineInfo.State.SUCCEEDED
                            }
                    if (!dependenciesMet) continue

                    // Check constraints
                    if (!constraints.match(workerEntity.constraints.toDomain())) {
                        dao.update(workerEntity.copy(state = LorraineInfo.State.BLOCKED))
                        continue
                    }

                    val worker =
                            LorraineWorker(
                                    workerUuid = Uuid.parse(workerEntity.uuid),
                                    application = application,
                                    platform = this
                            )
                    worker.execute()
                }
            }

    override suspend fun enqueue(
            queueId: String,
            type: ExistingLorrainePolicy,
            lorraineRequest: LorraineRequest
    ) {
        requireNotNull(operation.operations.firstOrNull()) {
            "Operations should not be empty"
        }
        val queue = queues.getOrElse(queueId) { createQueue(queueId) }
        var previous: NSOperation? = null
        val uuid = Uuid.random()
        val worker = createWorkerEntity(uuid = uuid, queueId = queueId, request = lorraineRequest)

        dao.insert(worker)
        scheduleBackgroundTask()
        processQueue()
    }

    override suspend fun enqueue(queueId: String, operation: LorraineOperation) {
        requireNotNull(operation.operations.firstOrNull()) { "Operations should not be empty" }

        val workers =
                operation.operations.map { op ->
                    createWorkerEntity(
                            uuid = Uuid.random(),
                            queueId = queueId,
                            request = op.request
                    )
                }

        dao.insert(workers)
        scheduleBackgroundTask()
        processQueue()
    }

    internal fun constraintChanged() {
        scope.launch { processQueue() }
    }

    override suspend fun cancelWorkById(uuid: Uuid) {
        val worker = dao.getWorker(uuid.toHexString()) ?: return
        dao.update(worker.copy(state = LorraineInfo.State.CANCELLED))
    }

    override suspend fun cancelUniqueWork(queueId: String) {
        dao.getWorkers().filter { it.queueId == queueId }.forEach {
            cancelWorkById(Uuid.parse(it.uuid))
        }
    }

    override suspend fun cancelAllWorkByTag(tag: String) {
        dao.getWorkers().filter { it.tags.contains(tag) }.forEach {
            cancelWorkById(Uuid.parse(it.uuid))
        }
    }

    override suspend fun cancelAllWork() {
        dao.getWorkers().forEach { cancelWorkById(Uuid.parse(it.uuid)) }
    }

    override suspend fun pruneWork() {
        dao.delete(dao.getWorkers().filter { it.state.isFinished })
    }

    override fun listenLorrainesInfo(): Flow<List<LorraineInfo>> =
            dao.getWorkersAsFlow().map { list -> list.map { it.toInfo() } }
}
