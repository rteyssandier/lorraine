package io.dot.lorraine.constraint

import io.dot.lorraine.dsl.LorraineConstraints
import io.dot.lorraine.logger.LorraineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.Closeable
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.Network.nw_path_monitor_update_handler_t
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_t
import platform.darwin.dispatch_queue_create

internal class ConnectivityCheck(
    scope: CoroutineScope,
    onChange: () -> Unit,
    logger: LorraineLogger
) : ConstraintCheck {

    private val observer = AppleNetworkObserver()

    private val _value = MutableStateFlow(false)

    init {
        observer.setListener(
            object : NetworkObserver.Listener {
                override fun networkChanged(isOnline: Boolean) {
                    _value.update { isOnline }
                }
            }
        )

        scope.launch {
            _value.onEach { logger.info("ConnectivityCheck: $it") }.collect { onChange() }
        }
    }

    override suspend fun match(constraints: LorraineConstraints): Boolean {
        if (!constraints.requireNetwork)
            return true

        return _value.value
    }

}

internal class AppleNetworkObserver : NetworkObserver, nw_path_monitor_update_handler_t {
    var monitor: nw_path_monitor_t = null
    var listener: NetworkObserver.Listener? = null

    override fun close() {
        if (monitor != null) {
            nw_path_monitor_cancel(monitor)
        }
    }

    override fun setListener(listener: NetworkObserver.Listener) {
        check(monitor == null) {
            "Apollo: there can be only one listener"
        }
        monitor = nw_path_monitor_create()
        this.listener = listener
        nw_path_monitor_set_update_handler(monitor, this)
        nw_path_monitor_set_queue(monitor, dispatch_queue_create("NWPath", null))
        nw_path_monitor_start(monitor)
    }

    override fun invoke(p1: nw_path_t) {
        listener?.networkChanged((nw_path_get_status(p1) == nw_path_status_satisfied))
    }
}

internal interface NetworkObserver : Closeable {
    /**
     * Sets the listener
     *
     * Implementation must call [listener] shortly after [setListener] returns to let the callers know about the initial state.
     */
    fun setListener(listener: Listener)

    interface Listener {
        fun networkChanged(isOnline: Boolean)
    }
}
