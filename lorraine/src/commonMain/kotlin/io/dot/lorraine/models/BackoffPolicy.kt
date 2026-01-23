package io.dot.lorraine.models

import kotlin.time.Duration

/**
 * Only on Android
 */
@ConsistentCopyVisibility
data class BackoffLorrainePolicy internal constructor(
    val duration: Duration,
    val policy: Policy
) {

    enum class Policy {
        EXPONENTIAL,
        LINEAR
    }

}