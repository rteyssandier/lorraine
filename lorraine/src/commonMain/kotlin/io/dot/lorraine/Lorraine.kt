@file:OptIn(ExperimentalUuidApi::class)

package io.dot.lorraine

import io.dot.lorraine.dsl.LorraineOperation
import io.dot.lorraine.dsl.LorraineOperationDefinition
import io.dot.lorraine.dsl.LorraineRequest
import io.dot.lorraine.dsl.LorraineRequestDefinition
import io.dot.lorraine.dsl.lorraineOperation
import io.dot.lorraine.dsl.lorraineRequest
import io.dot.lorraine.models.ExistingLorrainePolicy
import io.dot.lorraine.models.LorraineInfo
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal const val LORRAINE_DATABASE = "lorraine.db"

/**
 * Instance of [Lorraine], to enqueue lorraine's workers
 */
class Lorraine private constructor(
    private val platform: Platform
) {

    /**
     * Enqueue a [LorraineRequest]
     *
     * @param queueId of the request
     * @param type to enqueue
     * @param request, actual request
     */
    suspend fun enqueue(
        queueId: String,
        type: ExistingLorrainePolicy,
        request: LorraineRequest
    ) {
        platform.enqueue(
            queueId = queueId,
            type = type,
            lorraineRequest = request
        )
    }

    /**
     * Enqueue a [LorraineOperation] that contains multiple [LorraineRequest]
     *
     * @param uniqueId for the queue
     * @param operation to enqueue
     */
    suspend fun enqueue(
        queueId: String,
        operation: LorraineOperation
    ) {
        platform.enqueue(
            queueId = queueId,
            operation = operation
        )
    }

    suspend fun cancelWorkById(uuid: Uuid) {
        platform.cancelWorkById(uuid)
    }

    suspend fun cancelUniqueWork(queueId: String) {
        platform.cancelUniqueWork(queueId)
    }

    suspend fun cancelAllWorkByTag(tag: String) {
        platform.cancelAllWorkByTag(tag)
    }

    suspend fun cancelAllWork() {
        platform.cancelAllWork()
    }

    suspend fun pruneWork() {
        platform.pruneWork()
    }

    fun listenLorrainesInfo(): Flow<List<LorraineInfo>> {
        return platform.listenLorrainesInfo()
    }

    companion object {

        internal fun create(
            platform: Platform
        ): Lorraine {
            return Lorraine(platform = platform)
        }

    }

}

suspend fun Lorraine.enqueue(
    queueId: String,
    type: ExistingLorrainePolicy,
    block: LorraineRequestDefinition.() -> Unit
) {
    enqueue(
        queueId = queueId,
        type = type,
        request = lorraineRequest(block)
    )
}

suspend fun Lorraine.enqueue(
    queueId: String,
    block: LorraineOperationDefinition.() -> Unit
) {
    enqueue(
        queueId = queueId,
        operation = lorraineOperation(block)
    )
}