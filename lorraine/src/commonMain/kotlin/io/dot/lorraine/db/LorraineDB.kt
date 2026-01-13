package io.dot.lorraine.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.dot.lorraine.db.converter.DataConverter
import io.dot.lorraine.db.converter.StringSetConverter
import io.dot.lorraine.db.converter.UuidConverter
import io.dot.lorraine.db.dao.WorkerDao
import io.dot.lorraine.db.entity.WorkerEntity

@Database(
    version = 3,
    entities = [
        WorkerEntity::class
    ]
)
@TypeConverters(
    StringSetConverter::class,
    DataConverter::class,
    UuidConverter::class
)
@ConstructedBy(LorraineConstructor::class)
abstract class LorraineDB : RoomDatabase() {

    internal abstract fun workerDao(): WorkerDao

}