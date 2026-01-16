package io.dot.lorraine.mapping

import androidx.work.Constraints
import androidx.work.NetworkType
import io.dot.lorraine.dsl.LorraineConstraints

internal fun LorraineConstraints.asWorkManagerConstraints(): Constraints {
    return Constraints(
        requiredNetworkType = if (requireNetwork) {
            NetworkType.CONNECTED
        } else {
            NetworkType.NOT_REQUIRED
        },
        requiresBatteryNotLow = requireBatteryNotLow,
        requiresCharging = requireCharging
    )
}