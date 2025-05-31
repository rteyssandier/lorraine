@file:OptIn(ExperimentalForeignApi::class)

package io.dot.lorraine.db

import androidx.room.Room
import androidx.room.RoomDatabase
import io.dot.lorraine.LORRAINE_DATABASE
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal fun createDatabaseBuilder(): RoomDatabase.Builder<LorraineDB> {
    val parent = NSFileManager.defaultManager()
        .URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, false, null)
    val dbFilePath =
        "${parent?.path()}/$LORRAINE_DATABASE"

    return Room.databaseBuilder<LorraineDB>(
        name = dbFilePath
    )
}
