package app.onem.data.network.request

import com.google.gson.annotations.SerializedName

data class CapturePaymentIntentRequest(
    val merchantUsername: String,
    @SerializedName("payment_intent_id")
    val paymentIntentId: String
)
