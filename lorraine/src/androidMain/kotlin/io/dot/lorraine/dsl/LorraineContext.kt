package io.dot.lorraine.dsl

import android.content.Context
import io.dot.lorraine.models.AndroidLorraineContext

fun createLorraineContext(context: Context): AndroidLorraineContext {
    return AndroidLorraineContext.create(context)
}