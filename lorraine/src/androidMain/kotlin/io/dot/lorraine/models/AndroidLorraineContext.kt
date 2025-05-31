package io.dot.lorraine.models

import android.content.Context
import androidx.room.RoomDatabase
import androidx.work.WorkManager
import io.dot.lorraine.AndroidPlatform
import io.dot.lorraine.Platform
import io.dot.lorraine.db.LorraineDB
import io.dot.lorraine.db.getDatabaseBuilder

class AndroidLorraineContext private constructor(
    private val workManager: WorkManager,
    private val builder: RoomDatabase.Builder<LorraineDB>
) : LorraineContext() {

    override fun createDatabaseBuilder(): RoomDatabase.Builder<LorraineDB> {
        return builder
    }

    /**
     * Maybe find a better way to access dao & definitions in Worker
     */
    override fun createPlatform(application: LorraineApplication): Platform {
        AndroidPlatform.application = application

        return AndroidPlatform(
            workManager = workManager,
            application = application
        )
    }


    companion object {

        fun create(
            androidContext: Context
        ): AndroidLorraineContext {
            val builder = getDatabaseBuilder(androidContext)
            val workManager = WorkManager.getInstance(androidContext)

            return AndroidLorraineContext(builder = builder, workManager = workManager)
        }

    }
}