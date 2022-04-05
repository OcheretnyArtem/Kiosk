package app.onem.kioskandroid.feature.setup

import android.content.res.AssetManager
import app.onem.domain.usecase.reader.GetSavedReaderSerialNumberUseCase
import app.onem.domain.usecase.reader.SaveSelectedReaderSerialNumberUseCase
import app.onem.domain.usecase.sensors.GetSavedLocationIdUseCase
import app.onem.domain.usecase.sensors.SaveLocationIdUseCase
import app.onem.domain.usecase.shop.GetSelectedShopUseCase
import app.onem.domain.usecase.shop.SaveSelectedShopUseCase
import app.onem.domain.utils.Shop
import app.onem.kioskandroid.base.BaseViewModel
import app.onem.kioskandroid.base.PermissionState
import app.onem.kioskandroid.feature.setup.SetupAction.AlertDialogAction
import app.onem.kioskandroid.feature.setup.alert.SetupAlertType
import app.onem.kioskandroid.monitor.BluetoothMonitor
import app.onem.kioskandroid.monitor.LocationMonitor
import app.onem.kioskandroid.monitor.NetworkMonitor
import app.onem.kioskandroid.utils.CopyUtils
import app.onem.kioskandroid.utils.TerminalUtils
import app.onem.kioskandroid.utils.WebAppTools
import app.onem.kioskandroid.utils.allTrue
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class SetupViewModel(
    locationMonitor: LocationMonitor,
    networkMonitor: NetworkMonitor,
    bluetoothMonitor: BluetoothMonitor,
    private val getSavedReaderSerialNumberUseCase: GetSavedReaderSerialNumberUseCase,
    private val saveReaderSerialNumberUseCase: SaveSelectedReaderSerialNumberUseCase,
    private val getSelectedShopUseCase: GetSelectedShopUseCase,
    private val saveSelectedShopUseCase: SaveSelectedShopUseCase,
    private val saveLocationIdUseCase: SaveLocationIdUseCase,
    private val getSavedLocationIdUseCase: GetSavedLocationIdUseCase,
) : BaseViewModel<SetupViewState, SetupAction>() {

    private var selectedReader: Reader? = null
    private var selectedShop: Shop? = null
    private var selectedLocation: Location? = null

    override fun initialViewState() = SetupViewState()

    private val locationPermissionState: MutableSharedFlow<PermissionState> =
        MutableSharedFlow()

    private val bluetoothPermissionState: MutableSharedFlow<PermissionState> =
        MutableSharedFlow()

    private val allPermissionsGranted: Flow<Boolean> =
        combine(
            listOf(
                locationPermissionState,
                bluetoothPermissionState
            ),
            PermissionState::allGranted
        )

    private val locationStatus: SharedFlow<Boolean> =
        locationMonitor
            .locationStatus
            .shareIn(this, SharingStarted.Lazily, replay = 1)

    private val networkStatus: SharedFlow<Boolean> =
        networkMonitor
            .networkStatus
            .shareIn(this, SharingStarted.Lazily, replay = 1)

    private val bluetoothStatus: SharedFlow<Boolean> =
        bluetoothMonitor
            .bluetoothStatus
            .shareIn(this, SharingStarted.Lazily, replay = 1)

    private val allResourcesAvailable: Flow<Boolean> =
        combine(
            listOf(
                locationStatus,
                networkStatus,
                bluetoothStatus
            ),
            Boolean::allTrue
        )

    init {
        locationPermissionState
            .map { state ->
                chooseAlertAction(state, SetupAlertType.LOCATION_PERMISSION)
            }
            .onEach(this::sendAction)
            .launchIn(this)

        bluetoothPermissionState
            .map { state ->
                chooseAlertAction(state, SetupAlertType.BLUETOOTH_PERMISSION)
            }
            .onEach(this::sendAction)
            .launchIn(this)

        locationStatus
            .map { isAvailable ->
                chooseAlertAction(isAvailable, SetupAlertType.LOCATION)
            }
            .onEach(this::sendAction)
            .launchIn(this)

        networkStatus
            .map { isAvailable ->
                chooseAlertAction(isAvailable, SetupAlertType.INTERNET)
            }
            .onEach(this::sendAction)
            .launchIn(this)

        bluetoothStatus
            .map { isAvailable ->
                chooseAlertAction(isAvailable, SetupAlertType.BLUETOOTH)
            }
            .onEach(this::sendAction)
            .launchIn(this)

        combine(allPermissionsGranted, allResourcesAvailable, Boolean::and)
            .distinctUntilChanged()
            .onEach { everythingAvailable ->
                if (everythingAvailable) {
                    reconnectIfNeeded()
                }
            }
            .launchIn(this)
    }

    private fun chooseAlertAction(
        permissionState: PermissionState,
        alertType: SetupAlertType
    ): AlertDialogAction =
        when (permissionState) {
            PermissionState.GRANTED -> AlertDialogAction.Hide(alertType)
            PermissionState.DENIED,
            PermissionState.EXPLAINED -> AlertDialogAction.Show(alertType)
        }

    private fun chooseAlertAction(
        isResourceAvailable: Boolean,
        alertType: SetupAlertType
    ): AlertDialogAction =
        if (isResourceAvailable) {
            AlertDialogAction.Hide(alertType)
        } else {
            AlertDialogAction.Show(alertType)
        }

    internal fun copyWebApp(assetManager: AssetManager, indexFile: File, webAppDir: String) {
        launch {
            reduceViewState { it.copy(isLoading = true) }
            withContext(Dispatchers.IO) {
                if (!indexFile.exists()) {
                    val copy = CopyUtils()
                    copy.copyFileOrDir(assetManager, WebAppTools.webAppName, webAppDir)
                }
            }
            reduceViewState { it.copy(isLoading = false) }
        }
    }

    internal fun onChooseShopClicked() {
        sendAction(SetupAction.ShowChooseShopAction)
    }

    internal fun onTerminalSetupClicked() {
        sendAction(SetupAction.ShowTerminalAction)
    }

    internal fun onShopSelected(shop: Shop?) {
        selectedShop = shop
        launch {
            saveSelectedShopUseCase(selectedShop)
        }
        shop?.code?.let {
            sendAction(SetupAction.InitTerminal(it))
        }
        reduceViewState { it.copy(shop = selectedShop) }
    }

    internal fun onReaderSelected(reader: Reader?) {
        selectedReader = reader
        launch {
            saveReaderSerialNumberUseCase(selectedReader)
        }
        checkAvailability()
        reduceViewState { it.copy(reader = selectedReader) }
    }

    private fun reconnectIfNeeded() {
        if (selectedShop == null || selectedReader == null) {
            getSavedShop { shop ->
                if (shop != null) {
                    launch {
                        getSavedLocation { location ->
                            selectedLocation = location
                        }
                        getSavedReaderSerialNumberUseCase().collect { readerSerialNumber ->
                            if (readerSerialNumber != null &&
                                Terminal.isInitialized() &&
                                Terminal.getInstance().connectedReader == null
                            ) {
                                sendAction(SetupAction.Reconnect)
                            }
                        }
                    }
                }
            }
        }
    }

    internal fun disconnectReader() {
        launch {
            reduceViewState {
                it.copy(isLoading = true)
            }
            TerminalUtils.disconnectReader(
                onSuccess = {
                    onReaderSelected(null)
                    reduceViewState { it.copy(isLoading = false) }
                },
                onFailure = { e ->
                    Timber.e(e)
                    reduceViewState { it.copy(isLoading = false) }
                })
        }
    }

    internal fun onLocationPermissionResponse(permissionState: PermissionState) {
        launch {
            locationPermissionState.emit(permissionState)
        }
    }

    internal fun onBluetoothPermissionResponse(permissionState: PermissionState) {
        launch {
            bluetoothPermissionState.emit(permissionState)
        }
    }

    private fun checkAvailability() {
        if (selectedReader != null && selectedShop != null) {
            sendAction(SetupAction.AllChosen)
        }
    }

    private fun getSavedShop(onResult: (Shop?) -> Unit) {
        launch {
            getSelectedShopUseCase().collect {
                onShopSelected(it)
                onResult(it)
            }
        }
    }

    private fun getSavedLocation(onResult: (Location?) -> Unit) {
        launch {
            getSavedLocationIdUseCase().collect { locationId ->
                locationId?.let {
                    onResult(TerminalUtils.findLocationById(it))
                }
            }
        }
    }

    internal fun onLocationSelected(location: Location?) {
        launch {
            selectedLocation = location
            saveLocationIdUseCase(selectedLocation)
        }
    }

    internal fun onReaderInfoClicked() {
        viewStateFlow.value.reader?.let {
            sendAction(SetupAction.ShowReaderInfoDialog(it))
        }
    }
}
