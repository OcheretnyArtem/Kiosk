package app.onem.domain.usecase.model

data class RetrievePaymentIntentModel(
    val merchantUsername: String,
    val paymentIntentId: String
)
