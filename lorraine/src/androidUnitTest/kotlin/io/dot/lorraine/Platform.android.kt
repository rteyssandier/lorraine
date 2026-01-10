package io.dot.lorraine

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.dot.lorraine.db.LorraineDB

actual fun createDatabase(): LorraineDB {
    val context = ApplicationProvider.getApplicationContext<Context>()

    return Room.inMemoryDatabaseBuilder(
        context = context,
        klass = LorraineDB::class.java
    )
        .build()
}