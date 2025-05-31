package io.dot.lorraine.db.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import io.dot.lorraine.db.entity.BooleanData
import io.dot.lorraine.db.entity.DataEntity
import io.dot.lorraine.db.entity.DoubleData
import io.dot.lorraine.db.entity.FloatData
import io.dot.lorraine.db.entity.IntData
import io.dot.lorraine.db.entity.LongData
import io.dot.lorraine.db.entity.StringData
import io.dot.lorraine.db.entity.UnknownData
import io.dot.lorraine.work.LorraineData
import io.dot.lorraine.work.workData
import kotlinx.serialization.json.Json

@ProvidedTypeConverter
internal class DataConverter(
    private val json: Json
) {

    @TypeConverter
    fun typeFromJson(value: String): LorraineData {
        val list = json.decodeFromString<List<DataEntity>>(value)

        return workData {
            list.forEach { entity ->
                when (entity) {
                    is DoubleData -> put(entity.key, entity.value)
                    is FloatData -> put(entity.key, entity.value)
                    is IntData -> put(entity.key, entity.value)
                    is LongData -> put(entity.key, entity.value)
                    is StringData -> put(entity.key, entity.value)
                    is BooleanData -> put(entity.key, entity.value)
                    UnknownData -> Unit
                }
            }
        }
    }

    @TypeConverter
    fun typeToJson(data: LorraineData): String {
        val mapped: List<DataEntity> = data.map
            .mapNotNull { entry ->
                when (val value = entry.value) {
                    is Int -> IntData(
                        key = entry.key,
                        value = value
                    )

                    is Long -> LongData(
                        key = entry.key,
                        value = value
                    )

                    is Float -> FloatData(
                        key = entry.key,
                        value = value
                    )

                    is Double -> DoubleData(
                        key = entry.key,
                        value = value
                    )

                    is String -> StringData(
                        key = entry.key,
                        value = value
                    )

                    is Boolean -> BooleanData(
                        key = entry.key,
                        value = value
                    )

                    else -> null
                }
            }

        return json.encodeToString(mapped)
    }

}
