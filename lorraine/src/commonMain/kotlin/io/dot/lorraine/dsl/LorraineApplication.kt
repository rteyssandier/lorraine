package io.dot.lorraine.dsl

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.dot.lorraine.Lorraine
import io.dot.lorraine.db.converter.DataConverter
import io.dot.lorraine.db.converter.StringSetConverter
import io.dot.lorraine.db.entity.BooleanData
import io.dot.lorraine.db.entity.DataEntity
import io.dot.lorraine.db.entity.DoubleData
import io.dot.lorraine.db.entity.FloatData
import io.dot.lorraine.db.entity.IntData
import io.dot.lorraine.db.entity.LongData
import io.dot.lorraine.db.entity.StringData
import io.dot.lorraine.db.entity.UnknownData
import io.dot.lorraine.logger.DefaultLogger
import io.dot.lorraine.logger.Logger
import io.dot.lorraine.models.LorraineApplication
import io.dot.lorraine.models.LorraineContext
import io.dot.lorraine.models.LorraineLogger
import io.dot.lorraine.work.WorkLorraine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

typealias Instantiate<T> = () -> T

/**
 * Dsl method to initialize [Lorraine]
 */
fun startLorraine(
    context: LorraineContext,
    block: LorraineDefinition.() -> Unit
): Lorraine {
    val json = createJson()
    val lorraineDefinition = LorraineDefinition().apply(block)
    val database = context.createDatabaseBuilder()
        .fallbackToDestructiveMigration(dropAllTables = true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addTypeConverter(DataConverter(json))
        .addTypeConverter(StringSetConverter(json))
        .build()
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val logger = lorraineDefinition.createLogger()
    val platform = context.createPlatform(
        application = LorraineApplication(
            scope = scope,
            database = database,
            logger = logger,
            definitions = lorraineDefinition.definitions
        )
    )

    return Lorraine.create(platform = platform)
}

private fun createJson() = Json {
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        polymorphic(DataEntity::class) {
            subclass(IntData::class)
            subclass(LongData::class)
            subclass(DoubleData::class)
            subclass(FloatData::class)
            subclass(StringData::class)
            subclass(BooleanData::class)

            defaultDeserializer { UnknownData.serializer() }
        }
    }
}

class LorraineDefinition internal constructor() {

    internal val definitions = mutableMapOf<String, Instantiate<WorkLorraine>>()
    internal var loggerDefinition: LoggerDefinition? = null

    fun <T : WorkLorraine> work(identifier: String, create: Instantiate<T>) {
        definitions[identifier] = create
    }

    fun logger(block: LoggerDefinition.() -> Unit) {
        loggerDefinition = LoggerDefinition().apply(block)
    }

    internal fun createLogger(): LorraineLogger? = loggerDefinition?.createLogger()

}

class LoggerDefinition internal constructor() {
    // TODO Add level

    var enable: Boolean = false
    var logger: Logger = DefaultLogger

    internal fun createLogger() = LorraineLogger.create(
        enable = enable,
        logger = logger
    )
}


