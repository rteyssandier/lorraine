package io.dot.lorraine.models

import androidx.room.RoomDatabase
import io.dot.lorraine.Platform
import io.dot.lorraine.db.LorraineDB
import io.dot.lorraine.db.dao.WorkerDao
import io.dot.lorraine.dsl.LorraineDefinition
import kotlinx.coroutines.CoroutineScope

abstract class LorraineContext {

    internal abstract fun createDatabaseBuilder(): RoomDatabase.Builder<LorraineDB>

    internal abstract fun createPlatform(
        application: LorraineApplication
    ): Platform

}