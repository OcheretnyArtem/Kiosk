package app.onem.kioskandroid.feature.setup.reader

import app.onem.domain.usecase.shop.GetSelectedShopIdUseCase
import app.onem.domain.usecase.shop.GetSelectedShopUseCase
import app.onem.domain.utils.Shops
import app.onem.domain.utils.isTestShop
import app.onem.kioskandroid.base.BaseViewModel
import app.onem.kioskandroid.monitor.TerminalMonitor
import app.onem.kioskandroid.utils.EmulatorTools
import app.onem.kioskandroid.utils.TerminalUtils
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.DiscoveryMethod
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TerminalDialogViewModel(
    private val getSelectedShopIdUseCase: GetSelectedShopIdUseCase,
    private val terminalMonitor: TerminalMonitor
) : BaseViewModel<TerminalDialogViewState, TerminalDialogAction>() {

    private var isEmulator: Boolean = false

    internal var onLocationSelected: ((Location) -> Unit)? = null
    internal var onReaderSelected: ((Reader) -> Unit)? = null

    private var discoverCanceler: Cancelable? = null
    private val callbackObject: Callback = object : Callback {
        override fun onSuccess() {
            println("Finished discovering readers")
        }

        override fun onFailure(e: TerminalException) {
            e.printStackTrace()
        }
    }

    private val discoveryListener = object : DiscoveryListener {
        override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
            reduceViewState { it.copy(readersList = readers) }
        }
    }

    override fun initialViewState() = TerminalDialogViewState()

    internal fun readerSelected(item: ReaderItem) {
        launch {
            val state = viewStateFlow.value
            if (state.isEmulator || state.location != null || item.location != null) {
                TerminalUtils.connectToReader(
                    item.reader,
                    state.location ?: item.location,
                    terminalMonitor.input
                )
                item.location?.let { location ->
                    onLocationSelected?.invoke(location)
                }
                onReaderSelected?.invoke(item.reader)
                sendAction(TerminalDialogAction.HideDialog)
            }
            if (item.location == null) {
                sendAction(TerminalDialogAction.NoLocationForReader)
            }
        }
    }

    internal fun startDiscovering() {
        reduceViewState { it.copy(readersList = emptyList()) }
        launch {
            getSelectedShopIdUseCase().collect { shopId ->
                isEmulator = EmulatorTools.isEmulator()
                reduceViewState { it.copy(isEmulator = isEmulator) }
                val config =
                    DiscoveryConfiguration(0, DiscoveryMethod.BLUETOOTH_SCAN, isEmulator)
                if (Terminal.isInitialized()) {
                    discoverCanceler = Terminal.getInstance().discoverReaders(
                        config,
                        discoveryListener,
                        callbackObject
                    )
                }
            }
        }
    }

    internal fun onLocationClicked() {
        if (!viewStateFlow.value.isEmulator) {
            sendAction(TerminalDialogAction.ShowLocationDialog)
        }
    }

    internal fun onLocationSelected(location: Location) {
        onLocationSelected?.invoke(location)
        reduceViewState { it.copy(location = location) }
    }

    internal fun stopDiscovering() {
        discoverCanceler?.cancel(callbackObject)
    }
}
