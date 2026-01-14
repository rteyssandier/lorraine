package io.dot.lorraine.dsl

data class LorraineConstraints internal constructor(
    val requireNetwork: Boolean,
    val requireBatteryNotLow: Boolean,
    val requireCharging: Boolean,
) {

    companion object {
        val NONE = LorraineConstraints(
            requireNetwork = false,
            requireBatteryNotLow = false,
            requireCharging = false
        )
    }
}

class LorraineConstraintsDefinition internal constructor() {
    var requiredNetwork: Boolean = false
    var requiredBatteryNotLow: Boolean = false
    var requireCharging: Boolean = false


    fun build() = LorraineConstraints(
        requireNetwork = requiredNetwork,
        requireBatteryNotLow = requiredBatteryNotLow,
        requireCharging = requireCharging
    )

}