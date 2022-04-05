package app.onem.kioskandroid.feature.payment

import androidx.annotation.StringRes
import app.onem.domain.entities.Price
import app.onem.kioskandroid.R
import app.onem.kioskandroid.base.Action
import app.onem.kioskandroid.base.ViewState
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage
import com.stripe.stripeterminal.external.models.ReaderInputOptions

data class PaymentViewState(
    val price: Price? = null,
    val texts: List<Any> = emptyList(),
    val indicator: PaymentProgressIndicator = PaymentProgressIndicator.Hide,
) : ViewState

enum class PaymentProgressIndicator {
    Hide,
    ShowProgress,
    ShowCheckmark,
    ShowError
}

sealed class PaymentAction : Action {
    object Proceed : PaymentAction()
    data class SendPaymentResult(val paymentData: PaymentData) : PaymentAction()
}

fun initialState(price: Price): PaymentViewState = PaymentViewState(price = price)

fun PaymentViewState.displaySuccess(cardDetails: String?) =
    PaymentViewState(
        texts = preserveRemoveCardInstruction(this.texts, cardDetails),
        indicator = PaymentProgressIndicator.ShowCheckmark
    )

fun PaymentViewState.displayError(message: String?) =
    PaymentViewState(
        indicator = PaymentProgressIndicator.ShowError,
        texts = preserveRemoveCardInstruction(this.texts, message ?: "Unknown error")
    )

fun PaymentViewState.displayError(@StringRes errorResId: Int) =
    PaymentViewState(
        indicator = PaymentProgressIndicator.ShowError,
        texts = preserveRemoveCardInstruction(this.texts, errorResId)
    )

fun PaymentViewState.displayStep(@StringRes step: Int): PaymentViewState {
    val indicator = when(step) {
        R.string.preparingPayment,
        R.string.payment_step_process,
        R.string.payment_step_capture -> PaymentProgressIndicator.ShowProgress
        else -> PaymentProgressIndicator.Hide
    }

    val texts = when(step) {
        R.string.payment_step_process, R.string.payment_step_capture -> preserveRemoveCardInstruction(this.texts, step)
        else -> listOf(step)
    }

    return this.copy(
        texts = texts,
        indicator = indicator
    )
}

fun PaymentViewState.displayInputRequest(request: ReaderInputOptions): PaymentViewState =
    // no way to get actual input options from terminal SDK !!!!
    this.copy(
        texts = listOf(R.string.insertSwipeOrTapToPay),
        indicator = PaymentProgressIndicator.Hide
    )

fun PaymentViewState.displayReaderMessage(message: ReaderDisplayMessage): PaymentViewState {
    val readerMessageTextResId = when(message) {
        ReaderDisplayMessage.TRY_ANOTHER_CARD -> R.string.reader_message_try_another_card
        ReaderDisplayMessage.TRY_ANOTHER_READ_METHOD -> R.string.reader_message_try_another_read_method
        ReaderDisplayMessage.MULTIPLE_CONTACTLESS_CARDS_DETECTED -> R.string.reader_message_multiple_cards
        ReaderDisplayMessage.REMOVE_CARD -> R.string.reader_message_remove_card
        ReaderDisplayMessage.SWIPE_CARD -> R.string.reader_message_swipe
        ReaderDisplayMessage.INSERT_OR_SWIPE_CARD -> R.string.reader_message_insert_or_swipe
        ReaderDisplayMessage.INSERT_CARD -> R.string.reader_message_insert
        ReaderDisplayMessage.RETRY_CARD -> R.string.reader_message_retry
        ReaderDisplayMessage.CHECK_MOBILE_DEVICE -> R.string.reader_message_check_mobile
    }
    return this.copy(
        texts = listOf(readerMessageTextResId),
        indicator = PaymentProgressIndicator.Hide
    )
}



/**
 * Text - @StringRes or String
 */
@OptIn(ExperimentalStdlibApi::class)
private fun preserveRemoveCardInstruction(currentText: List<Any>, text: Any?) =
    buildList {
        if (currentText[0] == R.string.reader_message_remove_card)
            add(R.string.reader_message_remove_card)
        text?.let {
            add(it)
        }
    }



