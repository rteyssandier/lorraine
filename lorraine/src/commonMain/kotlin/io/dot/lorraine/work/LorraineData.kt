package io.dot.lorraine.work

/**
 * Class use to store input or output data work for [WorkLorraine]
 */
@ConsistentCopyVisibility
data class LorraineData internal constructor(
    internal val map: Map<String, Any?>
) {

    /**
     * Get a [Int] for given key
     *
     * @return a [Int] for the given key or null if not found or wrong type
     */
    fun getInt(key: String) = map[key]?.toString()
        ?.toIntOrNull()

    /**
     * Get a [Long] for given key
     *
     * @return a [Long] for the given key or null if not found or wrong type
     */
    fun getLong(key: String) = map[key]?.toString()
        ?.toLongOrNull()

    /**
     * Get a [Float] for given key
     *
     * @return a [Float] for the given key or null if not found or wrong type
     */
    fun getFloat(key: String) = map[key]?.toString()
        ?.toFloatOrNull()

    /**
     * Get a [Double] for given key
     *
     * @return a [Double] for the given key or null if not found or wrong type
     */
    fun getDouble(key: String) = map[key]?.toString()
        ?.toDoubleOrNull()

    /**
     * Get a [String] for given key
     *
     * @return a [String] for the given key or null if not found or wrong type
     */
    fun getString(key: String) = map[key]?.toString()

    /**
     * Get a [Boolean] for given key
     *
     * @return a [Boolean] for the given key or null if not found or wrong type
     */
    fun getBoolean(key: String) = map[key]?.toString()
        ?.toBooleanStrictOrNull()

}

fun dataOf(vararg arg: Pair<String, Any?>): LorraineData {
    val definition = DataDefinition()

    arg.forEach {
        definition.put(it.first, it.second)
    }

    return definition.build()
}

fun workData(block: DataDefinition.() -> Unit): LorraineData {
    return DataDefinition().apply(block)
        .build()
}

class DataDefinition internal constructor() {

    private val map: MutableMap<String, Any?> = mutableMapOf()

    fun put(key: String, value: Any?) {
        if (value == null) {
            map[key] = null
        } else {
            when (value) {
                is String,
                is Boolean,
                is Int,
                is Long,
                is Float,
                is Double -> map[key] = value

                else -> throw IllegalArgumentException("Key $key has invalid type ${value::class}")
            }
        }
    }

    internal fun build(): LorraineData {
        return LorraineData(map)
    }

}