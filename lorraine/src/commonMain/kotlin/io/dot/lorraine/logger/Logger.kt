package io.dot.lorraine.logger

interface Logger {

    fun info(message: String)
    fun error(message: String)

    companion object {

        const val TAG = "Lorraine"

    }

}

expect object DefaultLogger : Logger {
    override fun info(message: String)
    override fun error(message: String)


}
