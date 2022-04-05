package app.onem.kioskandroid.feature.informationcollection

import app.onem.domain.entities.Price
import app.onem.kioskandroid.base.Action
import app.onem.kioskandroid.base.ViewState
import app.onem.kioskandroid.feature.payment.PaymentData

data class InformationCollectionViewState(
    val isRequestProcess: Boolean = false
) : ViewState

sealed class InformationCollectionAction : Action {

    data class ConfirmPhone(val phone: String) : InformationCollectionAction()

    data class StartPayment(
        val price: Price,
        val name: String,
        val phone: String
    ) : InformationCollectionAction()

    data class SendPaymentResult(val paymentData: PaymentData) : InformationCollectionAction()
}
