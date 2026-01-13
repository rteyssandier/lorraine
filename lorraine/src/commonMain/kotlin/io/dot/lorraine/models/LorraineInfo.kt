@file:OptIn(ExperimentalUuidApi::class)

package io.dot.lorraine.models

import io.dot.lorraine.work.LorraineData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ConsistentCopyVisibility
data class LorraineInfo internal constructor(
    val uuid: Uuid,
    val identifier: String,
    val state: State,
    val inputData: LorraineData?,
    val outputData: LorraineData?,
    val tags: Set<String>
) {

    /**
     * Copy paste from WorkManager of Google
     */
    enum class State {
        /**
         * Used to indicate that the [WorkRequest] is enqueued and eligible to run when its
         * [Constraints] are met and resources are available.
         */
        ENQUEUED,

        /**
         * Used to indicate that the [WorkRequest] is currently being executed.
         */
        RUNNING,

        /**
         * Used to indicate that the [WorkRequest] has completed in a successful state.  Note
         * that [PeriodicWorkRequest]s will never enter this state (they will simply go back
         * to [.ENQUEUED] and be eligible to run again).
         */
        SUCCEEDED,

        /**
         * Used to indicate that the [WorkRequest] has completed in a failure state.  All
         * dependent work will also be marked as `#FAILED` and will never run.
         */
        FAILED,

        /**
         * Used to indicate that the [WorkRequest] is currently blocked because its
         * prerequisites haven't finished successfully.
         */
        BLOCKED,

        /**
         * Used to indicate that the [WorkRequest] has been cancelled and will not execute.
         * All dependent work will also be marked as `#CANCELLED` and will not run.
         */
        CANCELLED;

        /**
         * Returns `true` if this State is considered finished:
         * [.SUCCEEDED], [.FAILED], and * [.CANCELLED]
         */
        val isFinished: Boolean
            get() = this == SUCCEEDED || this == FAILED || this == CANCELLED
    }

}