package app.onem.domain.usecase.model

import app.onem.domain.entities.Price

data class CreatePaymentIntentModel(
    val merchantUsername: String,
    val amount: Price,
    val currency: String = "usd",
    val paymentMethodTypes: List<String> = listOf("card_present"),
    val captureMethod: String = "manual"
)
