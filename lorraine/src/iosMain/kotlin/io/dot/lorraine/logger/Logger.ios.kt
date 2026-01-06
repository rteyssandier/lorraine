package io.dot.lorraine.logger

actual object DefaultLogger : Logger {

    actual override fun info(message: String) {
        println("${Logger.TAG} - $message")
    }

    actual override fun error(message: String) {
        println("${Logger.TAG} - $message")
    }

}
