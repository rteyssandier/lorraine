package io.dot.lorraine.logger

import android.util.Log

actual object DefaultLogger : LorraineLogger {

    actual override fun info(message: String) {
        Log.i(LorraineLogger.TAG, message)
    }

    actual override fun error(message: String) {
        Log.e(LorraineLogger.TAG, message)
    }


}
