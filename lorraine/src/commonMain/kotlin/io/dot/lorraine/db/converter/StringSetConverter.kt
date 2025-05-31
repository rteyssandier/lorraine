package io.dot.lorraine.db.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import io.dot.lorraine.Lorraine
import kotlinx.serialization.json.Json

@ProvidedTypeConverter
internal class StringSetConverter(
    private val json: Json
) {

    @TypeConverter
    fun typeFromJson(value: String): Set<String> = json.decodeFromString(value)

    @TypeConverter
    fun typeToJson(list: Set<String>): String = json.encodeToString(list)

}