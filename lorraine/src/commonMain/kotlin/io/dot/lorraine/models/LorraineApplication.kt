@file:OptIn(ExperimentalUuidApi::class)

package io.dot.lorraine.models

import io.dot.lorraine.db.LorraineDB
import io.dot.lorraine.db.dao.WorkerDao
import io.dot.lorraine.dsl.Instantiate
import io.dot.lorraine.dsl.LorraineDefinition
import io.dot.lorraine.work.WorkLorraine
import kotlinx.coroutines.CoroutineScope
import kotlin.uuid.ExperimentalUuidApi

internal typealias WorkerDefinitions = Map<String, Instantiate<WorkLorraine>>

internal class LorraineApplication(
    val scope: CoroutineScope,
    val logger: LorraineLogger?,
    val database: LorraineDB,
    val definitions: WorkerDefinitions
)

internal val LorraineApplication.workerDao: WorkerDao
    get() = database.workerDao()