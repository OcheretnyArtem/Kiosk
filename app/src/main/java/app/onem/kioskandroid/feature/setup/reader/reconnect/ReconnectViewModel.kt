package app.onem.kioskandroid.feature.setup.reader.reconnect

import app.onem.domain.usecase.reader.GetSavedReaderSerialNumberUseCase
import app.onem.domain.usecase.sensors.GetSavedLocationIdUseCase
import app.onem.kioskandroid.base.BaseViewModel
import app.onem.kioskandroid.monitor.TerminalMonitor
import app.onem.kioskandroid.utils.EmulatorTools
import app.onem.kioskandroid.utils.TerminalUtils
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val RESULT_DELAY = 2000L

class ReconnectViewModel(
    private val getSavedReaderSerialNumberUseCase: GetSavedReaderSerialNumberUseCase,
    private val getSavedLocationIdUseCase: GetSavedLocationIdUseCase,
    private val terminalMonitor: TerminalMonitor
) : BaseViewModel<ReconnectViewState, ReconnectAction>() {
    override fun initialViewState() = ReconnectViewState()

    var onReaderReconnected: ((Reader?) -> Unit)? = null

    private val callbackObject: Callback = object : Callback {
        override fun onSuccess() {
            println("Finished discovering readers")
        }

        override fun onFailure(e: TerminalException) {
            e.printStackTrace()

            launch {
                reduceViewState {
                    it.copy(
                        isConnecting = false
                    )
                }
                delay(RESULT_DELAY)
                sendAction(ReconnectAction.Close)
            }
        }
    }

    fun reconnectToReader() {
        launch {
            checkConnectionData { location, reader ->
                if (reader != null && (location != null || EmulatorTools.isEmulator())) {
                    connectToReader(reader, location)
                }
            }
        }
    }

    private fun checkConnectionData(onResult: (Location?, Reader?) -> Unit) {
        if (EmulatorTools.isEmulator()) {
            getSavedReader {
                onResult(null, it)
            }
        } else {
            getSavedLocation { location ->
                getSavedReader { reader ->
                    onResult(location, reader)
                }
            }
        }
    }

    private fun getSavedLocation(onResult: (Location?) -> Unit) {
        launch {
            getSavedLocationIdUseCase().collect {
                onResult(it?.let { TerminalUtils.findLocationById(it) })
            }
        }
    }

    private fun getSavedReader(onResult: (Reader?) -> Unit) {
        launch {
            getSavedReaderSerialNumberUseCase().collect { readerId ->
                val config = DiscoveryConfiguration(
                    20,
                    DiscoveryMethod.BLUETOOTH_SCAN,
                    EmulatorTools.isEmulator()
                )
                if (Terminal.isInitialized()) {
                    Terminal.getInstance().discoverReaders(
                        config,
                        discoveryListener = object : DiscoveryListener {
                            override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
                                onResult(readers.find { reader -> reader.serialNumber == readerId })
                            }
                        },
                        callbackObject
                    )
                }
            }
        }
    }

    private fun connectToReader(reader: Reader, location: Location?) {
        launch {
            TerminalUtils.connectToReader(
                reader,
                location,
                terminalMonitor.input
            )
            reduceViewState {
                it.copy(
                    isConnecting = false,
                    reader = reader
                )
            }
            delay(RESULT_DELAY)
            onReaderReconnected?.invoke(reader)
            sendAction(ReconnectAction.Close)
        }
    }
}
