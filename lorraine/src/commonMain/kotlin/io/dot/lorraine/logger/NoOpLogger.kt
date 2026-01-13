package io.dot.lorraine.logger

internal object NoOpLogger : LorraineLogger {

    override fun info(message: String) {
        // No-op
    }

    override fun error(message: String) {
        // No-op
    }

}

