package io.dot.lorraine.db.entity

import androidx.room.ColumnInfo
import io.dot.lorraine.dsl.LorraineConstraints

internal data class ConstraintEntity(

    @ColumnInfo(name = "require_network")
    val requireNetwork: Boolean,
    @ColumnInfo(name = "require_battery_not_low")
    val requireBatteryNotLow: Boolean

)

internal fun ConstraintEntity.toDomain() = LorraineConstraints(
    requireNetwork = requireNetwork,
    requireBatteryNotLow = requireBatteryNotLow
)

internal fun LorraineConstraints.toEntity() = ConstraintEntity(
    requireNetwork = requireNetwork,
    requireBatteryNotLow = requireBatteryNotLow
)


