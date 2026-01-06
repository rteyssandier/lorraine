package io.dot.lorraine.logger

import android.util.Log

actual object DefaultLogger : Logger {

    actual override fun info(message: String) {
        Log.i(Logger.TAG, message)
    }

    actual override fun error(message: String) {
        Log.e(Logger.TAG, message)
    }


}
