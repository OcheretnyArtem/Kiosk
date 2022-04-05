package app.onem.kioskandroid.feature.informationcollection

import app.onem.domain.entities.Price
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.usecase.api.CreatePaymentIntentUseCase
import app.onem.domain.usecase.model.CreatePaymentIntentModel
import app.onem.kioskandroid.base.BaseViewModel
import app.onem.kioskandroid.feature.payment.PaymentData
import kotlinx.coroutines.launch

class InformationCollectionViewModel(
    private val price: Price,
    private val kioskRepository: KioskRepository,
    private val createPaymentIntentUseCase: CreatePaymentIntentUseCase,
) : BaseViewModel<InformationCollectionViewState, InformationCollectionAction>() {

    override fun initialViewState() = InformationCollectionViewState()

    private var name: String = ""
    private var phone: String = ""

    init {
        launch {
            val result = createPaymentIntentUseCase(
                CreatePaymentIntentModel(
                    merchantUsername = kioskRepository.selectedShop?.code ?: "",
                    amount = price
                )
            )
        }
    }

    internal fun onFieldsFilled(name: String, phone: String) {
        this.name = name
        // assuming that phone number data model is "+#(###)###-####",
        // because now we pass phone number to web app in this format and it works
        this.phone = phone.replace(" ", "")
        launch {
            sendAction(InformationCollectionAction.ConfirmPhone(phone = phone))
        }
    }

    internal fun onPhoneConfirmed() {
        launch {
            sendAction(
                InformationCollectionAction.StartPayment(
                    price = price,
                    name = name,
                    phone = phone
                )
            )
        }
    }

    internal fun paymentResultReceived(paymentData: PaymentData) {
        sendAction(InformationCollectionAction.SendPaymentResult(paymentData))
    }

}
