package app.onem.kioskandroid.monitor

import com.stripe.stripeterminal.external.callable.BluetoothReaderListener
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage
import com.stripe.stripeterminal.external.models.ReaderInputOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface TerminalMonitor {

    val readerInputRequest: SharedFlow<ReaderInputOptions>
    val message: SharedFlow<ReaderDisplayMessage>
    val input: BluetoothReaderListener

}
