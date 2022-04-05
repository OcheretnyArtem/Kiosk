package app.onem.kioskandroid.feature.webview

import app.onem.domain.entities.Price
import app.onem.domain.utils.Shop
import app.onem.kioskandroid.base.Action
import app.onem.kioskandroid.base.ViewState
import app.onem.kioskandroid.feature.payment.PaymentData

data class WebViewViewState(
    val shop: Shop? = null
) : ViewState

sealed class WebViewAction : Action {
    data class LoadShop(val shopId: String?) : WebViewAction()
    data class ShowSuccessScreen(val paymentData: PaymentData, val shopId: String?) : WebViewAction()
    data class ShowFailScreen(val paymentData: PaymentData, val shopId: String?) : WebViewAction()
    object ShopReceived : WebViewAction()
    object ShowSettingsScreen : WebViewAction()
    data class ShowInformationCollectionScreen(val price: Price) : WebViewAction()
}