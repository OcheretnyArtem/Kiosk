package app.onem.kioskandroid.feature.webview

import android.content.Context
import android.view.View
import androidx.lifecycle.viewModelScope
import app.onem.domain.entities.doubleToPrice
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.utils.Shop
import app.onem.kioskandroid.base.BaseViewModel
import app.onem.kioskandroid.feature.payment.PaymentData
import app.onem.kioskandroid.server.WebServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class WebViewViewModel(
    private val kioskRepository: KioskRepository
) : BaseViewModel<WebViewViewState, WebViewAction>() {

    override fun initialViewState() = WebViewViewState()
    private var shop: Shop? = null
    private var server: WebServer? = null
    private var singleClickJob: Job? = null
    internal var fragment: View? = null

    init {
        shop = kioskRepository.selectedShop
        sendAction(WebViewAction.ShopReceived)
    }

    internal fun runServer(webAppDirPath: String) {
//        server = WebServer("localhost", 3000, File(webAppDirPath).canonicalFile)
        server = WebServer("localhost", 3000, File(webAppDirPath).canonicalFile)
        launch {
            server?.start()
        }
        sendAction(WebViewAction.LoadShop(shop?.code))
    }

    internal fun stopServer() {
        server?.stop()
    }

    internal fun paymentResultReceived(paymentData: PaymentData) {
        launch {
            shop?.let {
                delay(1000)
                if (paymentData.isPaymentCompleted) {
                    sendAction(WebViewAction.ShowSuccessScreen(paymentData, it.code))
                } else {
                    sendAction(WebViewAction.ShowFailScreen(paymentData, it.code))
                }
            }
        }
    }

    internal fun onHomeButtonClicked() {
        if (singleClickJob != null) {
            singleClickJob?.cancel()
            singleClickJob = null
            sendAction(WebViewAction.ShowSettingsScreen)
        } else {
            singleClickJob = launch {
                delay(300L)
                sendAction(WebViewAction.LoadShop(shop?.code))
                singleClickJob = null
            }
        }
    }

    internal fun onPaymentClicked(price: Double) {
        launch(Dispatchers.Main) {
            sendAction(WebViewAction.ShowInformationCollectionScreen(doubleToPrice(price)))
        }
    }
}