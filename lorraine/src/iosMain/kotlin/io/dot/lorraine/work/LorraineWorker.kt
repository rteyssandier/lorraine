@file:OptIn(ExperimentalUuidApi::class)

package io.dot.lorraine.work

import io.dot.lorraine.Lorraine
import io.dot.lorraine.constraint.match
import io.dot.lorraine.db.entity.toDomain
import io.dot.lorraine.models.LorraineInfo
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSBlockOperation
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class LorraineWorker(
    private val workerUuid: Uuid
) : NSBlockOperation() {

//    override fun isAsynchronous(): Boolean = true

    override fun main() {
        runBlocking {
//            val dao = Lorraine.dao
//            val workerData = dao.getWorker(uuidString = workerUuid.toString()) ?: error("WorkLorraine not found")
//            val identifier = requireNotNull(workerData.identifier) { "Identifier not found" }
//            val workerDefinition = requireNotNull(Lorraine.definitions[identifier]) {
//                "Worker definition not found"
//            }
//
//            // TODO Check dependencies
//            if (!constraintChecks.match(workerData.constraints.toDomain())) {
//                dao.update(workerData.copy(state = LorraineInfo.State.BLOCKED))
//                return@runBlocking
//            }
//
//            val worker = workerDefinition.invoke()
//
//            dao.update(workerData.copy(state = LorraineInfo.State.RUNNING))
//
//            delay(3.seconds)
//
//            val result = worker.doWork(workerData.inputData)
//            val state = when (result) {
//                is LorraineResult.Failure -> {
//                    LorraineInfo.State.FAILED
//                }
//
//                is LorraineResult.Retry -> {
//                    // TODO Re-enqueue
//                    LorraineInfo.State.FAILED
//                }
//
//                is LorraineResult.Success -> {
//                    // TODO Delete worker if not in operation
//                    // TODO Delete all worker in operation, if all finish
//                    LorraineInfo.State.SUCCEEDED
//                }
//            }
//
//            dao.update(
//                workerData.copy(
//                    state = state,
//                    outputData = result.outputData
//                )
//            )
        }
    }

    override fun cancel() {
        // TODO Update worker state in db
//        runBlocking {
//            val dao = Lorraine.dao
//            val workerData = dao.getWorker(workerUuid.toString()) ?: error("WorkLorraine not found")
//
//            dao.update(workerData.copy(state = LorraineInfo.State.CANCELLED))
//
//            cancel()
//        }
        super.cancel()
    }

}