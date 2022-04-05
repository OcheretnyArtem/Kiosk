package app.onem.kioskandroid.feature.payment

import android.os.Parcelable
import app.onem.domain.entities.Price
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentData(
    val isPaymentCompleted: Boolean,
    val price: Price? = null,
    val name: String? = null,
    val phone: String? = null,
    val paymentIntentId: String? = null
) : Parcelable