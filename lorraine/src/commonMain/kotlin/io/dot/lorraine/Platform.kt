@file:OptIn(ExperimentalUuidApi::class)

package io.dot.lorraine

import io.dot.lorraine.dsl.LorraineOperation
import io.dot.lorraine.dsl.LorraineRequest
import io.dot.lorraine.models.ExistingLorrainePolicy
import io.dot.lorraine.models.LorraineInfo
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal interface Platform {
    val name: String

    suspend fun enqueue(
        queueId: String,
        type: ExistingLorrainePolicy,
        lorraineRequest: LorraineRequest
    )

    suspend fun enqueue(
        queueId: String,
        operation: LorraineOperation
    )

    suspend fun cancelWorkById(uuid: Uuid)

    suspend fun cancelUniqueWork(queueId: String)

    suspend fun cancelAllWorkByTag(tag: String)

    suspend fun cancelAllWork()

    suspend fun pruneWork()

    fun listenLorrainesInfo(): Flow<List<LorraineInfo>>

}