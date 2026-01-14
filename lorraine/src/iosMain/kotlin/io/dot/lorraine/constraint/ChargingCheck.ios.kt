package io.dot.lorraine.constraint

import io.dot.lorraine.dsl.LorraineConstraints
import io.dot.lorraine.logger.LorraineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.Closeable
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceBatteryState
import platform.UIKit.UIDeviceBatteryStateDidChangeNotification

internal class ChargingCheck(
    scope: CoroutineScope,
    onChange: () -> Unit,
    logger: LorraineLogger
) : ConstraintCheck {

    private val observer = AppleChargingObserver()

    private val _value = MutableStateFlow(false)

    init {
        observer.setListener(
            object : ChargingObserver.Listener {
                override fun chargingChanged(isCharging: Boolean) {
                    _value.update { isCharging }
                }
            }
        )

        scope.launch {
            _value.onEach { logger.info("ChargingCheck: $it") }.collect { onChange() }
        }
    }

    override suspend fun match(constraints: LorraineConstraints): Boolean {
        if (!constraints.requireCharging)
            return true

        return _value.value
    }

}

internal class AppleChargingObserver : ChargingObserver, Closeable {
    private var listener: ChargingObserver.Listener? = null
    private var stateObserver: Any? = null

    override fun setListener(listener: ChargingObserver.Listener) {
        this.listener = listener
        UIDevice.currentDevice.batteryMonitoringEnabled = true

        val center = NSNotificationCenter.defaultCenter

        stateObserver = center.addObserverForName(
            UIDeviceBatteryStateDidChangeNotification,
            null,
            NSOperationQueue.mainQueue
        ) { _ -> notifyListener() }

        notifyListener()
    }

    private fun notifyListener() {
        listener?.chargingChanged(isCharging())
    }

    private fun isCharging(): Boolean {
        val device = UIDevice.currentDevice
        val batteryState = device.batteryState

        return batteryState == UIDeviceBatteryState.UIDeviceBatteryStateCharging
    }

    override fun close() {
        stateObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        UIDevice.currentDevice.batteryMonitoringEnabled = false
    }
}

internal interface ChargingObserver : Closeable {
    /**
     * Sets the listener
     *
     * Implementation must call [listener] shortly after [setListener] returns to let the callers know about the initial state.
     */
    fun setListener(listener: Listener)

    interface Listener {
        fun chargingChanged(isCharging: Boolean)
    }
}
