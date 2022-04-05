package app.onem.kioskandroid.di

import app.onem.domain.entities.Price
import app.onem.kioskandroid.feature.informationcollection.InformationCollectionViewModel
import app.onem.kioskandroid.feature.main.MainViewModel
import app.onem.kioskandroid.feature.payment.PaymentViewModel
import app.onem.kioskandroid.feature.setup.SetupViewModel
import app.onem.kioskandroid.feature.setup.reader.TerminalDialogViewModel
import app.onem.kioskandroid.feature.setup.reader.create_location.CreateLocationViewModel
import app.onem.kioskandroid.feature.setup.reader.location.LocationViewModel
import app.onem.kioskandroid.feature.setup.reader.reconnect.ReconnectViewModel
import app.onem.kioskandroid.feature.setup.shop.ChooseShopViewModel
import app.onem.kioskandroid.feature.webview.WebViewViewModel
import app.onem.kioskandroid.monitor.BluetoothMonitor
import app.onem.kioskandroid.monitor.BluetoothMonitorImpl
import app.onem.kioskandroid.monitor.LocationMonitor
import app.onem.kioskandroid.monitor.LocationMonitorImpl
import app.onem.kioskandroid.monitor.NetworkMonitor
import app.onem.kioskandroid.monitor.NetworkMonitorImpl
import app.onem.kioskandroid.monitor.TerminalMonitor
import app.onem.kioskandroid.monitor.TerminalMonitorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val koinModule = module {
    viewModel { SetupViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ChooseShopViewModel(get()) }
    viewModel { TerminalDialogViewModel(get(), get()) }
    viewModel { WebViewViewModel(get()) }
    viewModel { (
                    price: Price,
                    name: String,
                    phone: String
                ) ->
        PaymentViewModel(price, name, phone, get(), get(), get(), get())
    }
    viewModel { (price: Price) -> InformationCollectionViewModel(price, get(), get()) }
    viewModel { ReconnectViewModel(get(), get(), get()) }
    viewModel { LocationViewModel() }
    viewModel { CreateLocationViewModel(get(), get()) }
    viewModel { MainViewModel() }
    single<LocationMonitor> { LocationMonitorImpl(androidContext()) }
    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }
    single<BluetoothMonitor> { BluetoothMonitorImpl(androidContext()) }
    single<TerminalMonitor> { TerminalMonitorImpl() }
}
