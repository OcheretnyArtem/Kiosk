package app.onem.data.network.request

import com.google.gson.annotations.SerializedName

data class CreatePaymentIntentRequest(
    val merchantUsername: String,
    val amount: Int,
    val currency: String,
    @SerializedName("payment_method_types")
    val paymentMethodTypes: List<String> = listOf("card_present"),
    @SerializedName("capture_method")
    val captureMethod: String = "manual"
)