package app.onem.kioskandroid.feature.payment

import app.onem.domain.core.result.Result
import app.onem.domain.entities.Price
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.usecase.api.CapturePaymentIntentUseCase
import app.onem.domain.usecase.api.CreatePaymentIntentUseCase
import app.onem.domain.usecase.model.CapturePaymentIntentModel
import app.onem.domain.usecase.model.CreatePaymentIntentModel
import app.onem.kioskandroid.R
import app.onem.kioskandroid.base.BaseViewModel
import app.onem.kioskandroid.monitor.TerminalMonitor
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.Cancelable
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.models.CardPresentDetails
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage
import com.stripe.stripeterminal.external.models.TerminalException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException

class PaymentViewModel(
    val price: Price,
    val name: String,
    val phone: String,
    private val kioskRepository: KioskRepository,
    private val createPaymentIntentUseCase: CreatePaymentIntentUseCase,
    private val capturePaymentIntentUseCase: CapturePaymentIntentUseCase,
    private val terminalMonitor: TerminalMonitor
) : BaseViewModel<PaymentViewState, PaymentAction>() {

    companion object {
        private const val MESSAGE_SHOW_DELAY = 3000L
        private const val APPROVED_MESSAGE_SHOW_DELAY = 1000L
    }

    private var paymentData: PaymentData? = null
    private var currentPaymentIntent: PaymentIntent? = null
    private var paymentStepJob: Job? = null

    private var collectTask: Cancelable? = null

    override fun initialViewState() = PaymentViewState()

    init {
        initializeStep()
        launch {
            terminalMonitor.message.collect { message ->
                reduceViewState {
                    it.displayReaderMessage(message)
                }
            }
        }
        launch {
            terminalMonitor.readerInputRequest.collect { request ->
                reduceViewState {
                    it.displayInputRequest(request)
                }
            }
        }
    }

    private fun initializeStep() {
        paymentData = PaymentData(
            isPaymentCompleted = false,
            price = price,
            name = name,
            phone = phone
        )
        paymentStepJob = launch {
            reduceViewState {
                initialState(price).displayStep(R.string.preparingPayment)
            }
            val result = createPaymentIntentUseCase(
                CreatePaymentIntentModel(
                    merchantUsername = kioskRepository.selectedShop?.code ?: "",
                    amount = price
                )
            )

            handleResponse(result) {
                currentPaymentIntent = it.value
                startPaymentProcess()
            }
        }
    }

    private fun showErrorMessage(message: String?) {
        reduceViewState {
            it.displayError(message)
        }
    }

    private fun startPaymentProcess() {
        currentPaymentIntent?.let { paymentIntent ->
            collectTask = Terminal.getInstance()
                .collectPaymentMethod(paymentIntent, object : PaymentIntentCallback {
                    override fun onFailure(e: TerminalException) {
                        paymentStepJob = launch {
                            showErrorMessage(e.errorMessage)
                            delay(MESSAGE_SHOW_DELAY)
                            currentPaymentIntent?.let {
                                paymentData = updatePaymentData(it.id)
                                sendPaymentResult()
                            }
                        }
                    }

                    override fun onSuccess(paymentIntent: PaymentIntent) {
                        currentPaymentIntent = paymentIntent
                        processPayment()
                    }
                })
        }
    }

    private fun processPayment() {
        reduceViewState {
            it.displayStep(R.string.payment_step_process)
        }
        Terminal.getInstance()
            .processPayment(currentPaymentIntent!!, object : PaymentIntentCallback {
                override fun onFailure(e: TerminalException) {

                    paymentStepJob = launch {
                        paymentStepJob = launch {
                            showErrorMessage(e.errorMessage)
                            delay(MESSAGE_SHOW_DELAY)
                            initializeStep()
                        }
                    }
                }

                override fun onSuccess(paymentIntent: PaymentIntent) {
                    currentPaymentIntent = paymentIntent
                    capturePayment()
                }
            })
    }

    private fun capturePayment() {
        reduceViewState {
            it.displayStep(R.string.payment_step_capture)
        }
        paymentStepJob = launch {
            if (kioskRepository.selectedShop != null && currentPaymentIntent != null) {
                val capturePaymentResult = capturePaymentIntentUseCase(
                    CapturePaymentIntentModel(
                        merchantUsername = kioskRepository.selectedShop!!.code ?: "",
                        paymentIntentId = currentPaymentIntent!!.id
                    )
                )
                handleResponse(capturePaymentResult) {
                    paymentData = updatePaymentData(it.value.intent)
                    paymentApproved()
                }
            }
        }
    }

    private fun paymentApproved() {
        paymentStepJob = launch {
            val cardDetails: CardPresentDetails? =
                currentPaymentIntent?.getCharges()?.firstOrNull()?.paymentMethodDetails?.cardPresentDetails
            val cardDetailsText = cardDetails?.let { "${it.brand?.uppercase()} ${it.last4}" }
            reduceViewState {
                it.displaySuccess(cardDetailsText)
            }
            delay(APPROVED_MESSAGE_SHOW_DELAY)
            sendPaymentResult()
        }
    }

    private fun updatePaymentData(intentId: String) = paymentData?.copy(paymentIntentId = intentId)

    private fun sendPaymentResult(isPaymentCompleted: Boolean = true) {
        paymentData = paymentData?.copy(isPaymentCompleted = isPaymentCompleted)
        paymentData?.let {
            sendAction(PaymentAction.SendPaymentResult(it))
        }
    }

    internal fun onCancelClicked() {
        paymentStepJob?.let {
            if (!it.isActive) {
                it.cancel()
            }
        }
        sendPaymentResult(isPaymentCompleted = false)
    }

    private fun <T> handleResponse(result: Result<T>, successHandler: (result: Result.Success<T>) -> Unit){
        when (result) {
            is Result.Success -> successHandler(result)
            is Result.Failure<*> -> {
                setErrorStateByRequest(result)
            }
        }
    }

    private fun setErrorStateByRequest(failure: Result.Failure<*>) {
        val errorResId = when(failure.error) {
            is IOException -> R.string.errorConnection
            else -> R.string.errorUnknown
        }
        Timber.e(failure.error, "payment request failed with")
        reduceViewState {
            it.displayError(errorResId)
        }
        paymentData?.let {
            paymentData = it.copy(
                isPaymentCompleted = false,
                paymentIntentId = currentPaymentIntent?.id
            )
        }
    }

    private fun cancelCollectPayment() {
        collectTask?.cancel(object: Callback {
            override fun onSuccess() {}

            override fun onFailure(e: TerminalException) {
                // TODO need a way to report exceptions
            }
        })
    }

    override fun onCleared() {
        cancelCollectPayment()
        super.onCleared()
    }
}

