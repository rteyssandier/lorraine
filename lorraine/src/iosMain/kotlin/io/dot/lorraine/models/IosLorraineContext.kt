package io.dot.lorraine.models

import androidx.room.RoomDatabase
import io.dot.lorraine.IOSPlatform
import io.dot.lorraine.Platform
import io.dot.lorraine.db.LorraineDB

class IosLorraineContext private constructor() : LorraineContext() {

    override fun createDatabaseBuilder(): RoomDatabase.Builder<LorraineDB> {
        return io.dot.lorraine.db.createDatabaseBuilder()
    }

    override fun createPlatform(application: LorraineApplication): Platform {
        return IOSPlatform(application = application)
    }

    companion object {

        fun create(): IosLorraineContext = IosLorraineContext()

    }
}