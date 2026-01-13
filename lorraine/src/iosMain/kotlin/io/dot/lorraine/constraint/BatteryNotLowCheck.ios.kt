package io.dot.lorraine.constraint

import io.dot.lorraine.dsl.LorraineConstraints
import io.dot.lorraine.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.Closeable
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceBatteryLevelDidChangeNotification
import platform.UIKit.UIDeviceBatteryState
import platform.UIKit.UIDeviceBatteryStateDidChangeNotification

internal class BatteryNotLowCheck(
    scope: CoroutineScope,
    onChange: () -> Unit,
    logger: Logger
) : ConstraintCheck {

    private val observer = AppleBatteryObserver()

    private val _value = MutableStateFlow(false)

    init {
        observer.setListener(
            object : BatteryObserver.Listener {
                override fun batteryChanged(isNotLow: Boolean) {
                    _value.update { isNotLow }
                }
            }
        )

        scope.launch {
            _value.onEach { logger.info("BatteryNotLowCheck: $it") }.collect { onChange() }
        }
    }

    override suspend fun match(constraints: LorraineConstraints): Boolean {
        if (!constraints.requireBatteryNotLow)
            return true

        return _value.value
    }

}

internal class AppleBatteryObserver : BatteryObserver, Closeable {
    private var listener: BatteryObserver.Listener? = null
    private var levelObserver: Any? = null
    private var stateObserver: Any? = null

    override fun setListener(listener: BatteryObserver.Listener) {
        this.listener = listener
        UIDevice.currentDevice.batteryMonitoringEnabled = true

        val center = NSNotificationCenter.defaultCenter

        levelObserver = center.addObserverForName(
            UIDeviceBatteryLevelDidChangeNotification,
            null,
            NSOperationQueue.mainQueue
        ) { _ -> notifyListener() }

        stateObserver = center.addObserverForName(
            UIDeviceBatteryStateDidChangeNotification,
            null,
            NSOperationQueue.mainQueue
        ) { _ -> notifyListener() }

        // Notify initial state
        notifyListener()
    }

    private fun notifyListener() {
        listener?.batteryChanged(isBatteryNotLow())
    }

    private fun isBatteryNotLow(): Boolean {
        val device = UIDevice.currentDevice
        val batteryLevel = device.batteryLevel
        val batteryState = device.batteryState

        // -1 means unknown (simulator), treat as not low
        if (batteryLevel < 0) return true

        // Battery is NOT low if:
        // - level is above 15%, OR
        // - device is charging, OR
        // - device is full
        return batteryLevel > 0.15f ||
                batteryState == UIDeviceBatteryState.UIDeviceBatteryStateCharging ||
                batteryState == UIDeviceBatteryState.UIDeviceBatteryStateFull
    }

    override fun close() {
        levelObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        stateObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        UIDevice.currentDevice.batteryMonitoringEnabled = false
    }
}

internal interface BatteryObserver : Closeable {
    /**
     * Sets the listener
     *
     * Implementation must call [listener] shortly after [setListener] returns to let the callers know about the initial state.
     */
    fun setListener(listener: Listener)

    interface Listener {
        fun batteryChanged(isNotLow: Boolean)
    }
}


