package io.dot.lorraine.logger

actual object DefaultLogger : LorraineLogger {

    actual override fun info(message: String) {
        println("${LorraineLogger.TAG} - $message")
    }

    actual override fun error(message: String) {
        println("${LorraineLogger.TAG} - $message")
    }

}
