package io.dot.lorraine.initializer

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import io.dot.lorraine.AndroidPlatform
import io.dot.lorraine.Lorraine
import io.dot.lorraine.db.getDatabaseBuilder

//@Suppress("unused")
//internal class LorraineInitializer : Initializer<Unit> {
//
//    override fun create(context: Context) {
//        if (!WorkManager.isInitialized()) {
//            WorkManager.initialize(
//                context,
//                Configuration.Builder()
//                    .build()
//            )
//        }
//
//        Lorraine.registerPlatform(
//            AndroidPlatform(WorkManager.getInstance(context)),
//            getDatabaseBuilder(context)
//        )
//    }
//
//    override fun dependencies(): List<Class<out Initializer<*>>> {
//        return emptyList()
//    }
//
//}