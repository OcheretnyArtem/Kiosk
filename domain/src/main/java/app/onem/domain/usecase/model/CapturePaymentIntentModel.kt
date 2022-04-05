package app.onem.domain.usecase.model

data class CapturePaymentIntentModel(
    val merchantUsername: String,
    val paymentIntentId: String
)