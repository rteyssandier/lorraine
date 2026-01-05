package io.dot.lorraine.constraint

import io.dot.lorraine.dsl.LorraineConstraints

/**
 * Use to check multiple constraint for a given worker
 *
 * TLDR, will not be used on Android, where everything will be passed to WorkManager
 */
internal interface ConstraintCheck {

    suspend fun match(constraints: LorraineConstraints): Boolean

}

internal suspend fun List<ConstraintCheck>.match(constraints: LorraineConstraints): Boolean {
    return all { it.match(constraints) }
}