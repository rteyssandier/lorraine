package io.dot.lorraine.models

import androidx.room.RoomDatabase
import io.dot.lorraine.IOSPlatform
import io.dot.lorraine.Platform
import io.dot.lorraine.db.LorraineDB
import io.dot.lorraine.db.dao.WorkerDao
import io.dot.lorraine.dsl.LorraineDefinition
import kotlinx.coroutines.CoroutineScope

class IosLorraineContext private constructor() : LorraineContext() {

    override fun createDatabaseBuilder(): RoomDatabase.Builder<LorraineDB> {
        return io.dot.lorraine.db.createDatabaseBuilder()
    }

    override fun createPlatform(
        coroutineScope: CoroutineScope,
        workerDao: WorkerDao,
        definitions: Map<String, LorraineDefinition>
    ): Platform {
        return IOSPlatform()
    }

    companion object {

        fun create(): IosLorraineContext = IosLorraineContext()

    }
}