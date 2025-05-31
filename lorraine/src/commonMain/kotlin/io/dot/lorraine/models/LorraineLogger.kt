package io.dot.lorraine.models

import io.dot.lorraine.logger.Logger

class LorraineLogger private constructor(
    private val enable: Boolean,
    private val logger: Logger
) {

    companion object {

        fun create(
            enable: Boolean,
            logger: Logger
        ) = LorraineLogger(
            enable = enable,
            logger = logger
        )

    }

}