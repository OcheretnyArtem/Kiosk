package app.onem.kioskandroid.monitor

import com.stripe.stripeterminal.external.callable.BluetoothReaderListener
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage
import com.stripe.stripeterminal.external.models.ReaderInputOptions
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class TerminalMonitorImpl : TerminalMonitor {

    private val _message = createEventBus<ReaderDisplayMessage>()
    private val _readerInputRequest = createEventBus<ReaderInputOptions>()

    override val message = _message.asSharedFlow()
    override val readerInputRequest = _readerInputRequest.asSharedFlow()

    override val input = object : BluetoothReaderListener {
        override fun onRequestReaderDisplayMessage(message: ReaderDisplayMessage) {
            _message.tryEmit(message)
        }

        override fun onRequestReaderInput(options: ReaderInputOptions) {
            _readerInputRequest.tryEmit(options)
        }
    }

}

private fun <T> createEventBus() = MutableSharedFlow<T>(
    0,
    5,
    BufferOverflow.DROP_OLDEST
)
