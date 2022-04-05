package app.onem.kioskandroid.feature.setup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import app.onem.domain.repositories.TokenProvider
import app.onem.kioskandroid.R
import app.onem.kioskandroid.base.BaseFragment
import app.onem.kioskandroid.base.permissions.Resource
import app.onem.kioskandroid.databinding.FrSetupBinding
import app.onem.kioskandroid.feature.setup.alert.SetupAlert
import app.onem.kioskandroid.feature.setup.alert.SetupAlertAction
import app.onem.kioskandroid.feature.setup.alert.SetupAlertType
import app.onem.kioskandroid.feature.setup.reader.TerminalDialog
import app.onem.kioskandroid.feature.setup.reader.reconnect.ReconnectDialog
import app.onem.kioskandroid.feature.setup.shop.ChooseShopDialog
import app.onem.kioskandroid.utils.TerminalUtils
import app.onem.kioskandroid.utils.getCroppedSerial
import app.onem.kioskandroid.utils.webAppDirFile
import app.onem.kioskandroid.utils.webAppIndexFile
import by.kirich1409.viewbindingdelegate.viewBinding
import com.stripe.stripeterminal.external.models.Reader
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel


class SetupFragment : BaseFragment(R.layout.fr_setup) {

    companion object {
        private val setupResources = listOf(
            Resource.LOCATION,
            Resource.BLUETOOTH
        )
    }

    private val viewModel by viewModel<SetupViewModel>()
    private val binding: FrSetupBinding by viewBinding()
    private val tokenProvider: TokenProvider by inject()

    private val permissionsResult: ActivityResultLauncher<List<Resource>> by requestPermissions(
        setupResources,
        onResult = { resultMap ->
            resultMap
                .forEach { (resource, permissionState) ->
                    when (resource) {
                        Resource.LOCATION -> viewModel.onLocationPermissionResponse(permissionState)
                        Resource.BLUETOOTH -> viewModel.onBluetoothPermissionResponse(permissionState)
                    }
                }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(requireContext()) {
            viewModel.copyWebApp(assets, webAppIndexFile, webAppDirFile.absolutePath)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setListeners()
    }

    override fun onStart() {
        super.onStart()
        permissionsResult.launch(setupResources)
    }

    private fun setListeners() {
        with(binding) {
            tvDiscover.setOnClickListener { viewModel.onTerminalSetupClicked() }
            tvShopTitle.setOnClickListener { viewModel.onChooseShopClicked() }
            tvBluetooth.setOnClickListener { }
            tvGoToShop.setOnClickListener { goToShop() }
            tvDisconnect.setOnClickListener { viewModel.disconnectReader() }
            tvShopSelected.setOnClickListener { viewModel.onChooseShopClicked() }
            tvReaderInfo.setOnClickListener { viewModel.onReaderInfoClicked() }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.viewStateFlow.collect(::setViewState)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.actionFlow.collect(::runAction)
        }
        lifecycleScope.launchWhenStarted {
            SetupAlert.actionFlow.collect(::runAlertAction)
        }
    }

    private fun setViewState(state: SetupViewState) {
        with(binding) {
            progressIndicator.isVisible = state.isLoading
            readerConnectedLayout.isVisible = state.reader != null
            selectStoreLayout.isVisible = state.reader == null

            tvShopSelected.text = state.shop?.value ?: getString(R.string.mainScreenShopTitle)

            tvReaderTitle.isVisible = state.shop != null
            tvDiscover.isVisible = state.shop != null

            tvReaderInfo.isVisible = state.reader != null
            ivLogo.isVisible = state.reader == null

            state.reader?.let {
                tvReaderInfo.text = resources.getString(
                    R.string.readerInfo,
                    it.deviceType.name,
                    it.getCroppedSerial()
                )
            }
        }
    }

    private fun runAction(action: SetupAction) {
        when (action) {
            is SetupAction.ShowChooseShopAction -> {
                ChooseShopDialog {
                    viewModel.onReaderSelected(null)
                    viewModel.onLocationSelected(null)
                    viewModel.onShopSelected(it)
                }.show(childFragmentManager, ChooseShopDialog::class.java.name)
            }
            SetupAction.ShowTerminalAction -> {
                TerminalDialog(
                    onLocationSelected = {
                        viewModel.onLocationSelected(it)
                        viewModel.onReaderSelected(null)
                    },
                    onTerminalSelected = { viewModel.onReaderSelected(it) }
                )
                    .show(childFragmentManager, TerminalDialog::class.java.name)
            }
            is SetupAction.InitTerminal -> {
                initTerminal(action.name)
            }
            SetupAction.Reconnect -> {
                ReconnectDialog { it?.let { viewModel.onReaderSelected(it) } }
                    .show(childFragmentManager, ReconnectDialog::class.java.name)
            }
            SetupAction.AllChosen -> {
                goToShop()
            }
            is SetupAction.AlertDialogAction.Show -> showAlert(action.alertType)
            is SetupAction.AlertDialogAction.Hide -> hideAlert(action.alertType)
            is SetupAction.ShowReaderInfoDialog -> {
                showReaderInfoDialog(action.reader)
            }
        }
    }

    private fun runAlertAction(action: SetupAlertAction) {
        when (action) {
            is SetupAlertAction.ButtonClicked -> {
                onAlertButtonClicked(action.alertType)
            }
        }
    }

    private fun onAlertButtonClicked(alertType: SetupAlertType) {
        when (alertType) {
            SetupAlertType.LOCATION_PERMISSION,
            SetupAlertType.BLUETOOTH_PERMISSION -> goToApplicationDetailsSettings()
            SetupAlertType.INTERNET -> goToWiFiSettings()
            SetupAlertType.BLUETOOTH -> goToBluetoothSettings()
            SetupAlertType.LOCATION -> throw IllegalArgumentException()
        }
    }

    private fun initTerminal(name: String) {
        tokenProvider.name = name
        TerminalUtils.initTerminal(tokenProvider, requireContext())
    }

    private fun showAlert(alertType: SetupAlertType) {
        if (findAlertOfType(alertType) == null) {
            SetupAlert
                .newInstance(alertType)
                .show(childFragmentManager, alertType.tag)
        }
    }

    private fun hideAlert(alertType: SetupAlertType) {
        findAlertOfType(alertType)?.dismiss()
    }

    private fun findAlertOfType(alertType: SetupAlertType): SetupAlert? =
        childFragmentManager.findFragmentByTag(alertType.tag) as SetupAlert?

    private fun goToShop() {
        navigate(SetupFragmentDirections.actionSetupFragmentToWebViewFragment())
    }

    private fun goToWiFiSettings() {
        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    private fun goToBluetoothSettings() {
        startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    private fun goToApplicationDetailsSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + requireContext().packageName)
            )
        )
    }

    private fun showReaderInfoDialog(reader: Reader) {
        AlertDialog.Builder(requireContext())
            .setTitle(reader.getCroppedSerial())
            .setMessage(
                resources.getString(
                    R.string.readerFirmwareInfo,
                    reader.firmwareVersion,
                    reader.configVersion
                )
            )
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}