package app.onem.domain.repositories

import app.onem.domain.core.result.Result
import app.onem.domain.usecase.model.IntentSecretModel
import app.onem.domain.utils.Shop
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.Reader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface KioskRepository {

    // Quickfix for WebView. Later - rewrite to Flow
    var selectedShop: Shop?

    val selectedShopFlow: SharedFlow<Shop?>
    val selectedReader: Flow<Reader>

    fun saveSelectedShop(shop: Shop?)
    fun saveSelectedReader(reader: Reader?)

    suspend fun getConnectionToken(name: String): Result<String>

    suspend fun createPaymentIntent(
        merchantUsername: String,
        amountInCents: Int,
        currency: String,
        paymentMethodTypes: List<String> = listOf("card_present"),
        captureMethod: String = "manual"
    ): Result<PaymentIntent>

    suspend fun retrievePaymentIntent(
        merchantUsername: String,
        paymentIntentId: String
    ): Result<PaymentIntent>

    suspend fun capturePaymentIntent(
        merchantUsername: String,
        paymentIntentId: String
    ): Result<IntentSecretModel>

    suspend fun registerReader(
        label: String,
        registrationCode: String,
        location: String,
        merchantUsername: String
    ): Result<Map<String, Any>>

    suspend fun createLocation(
        displayName: String,
        address: Map<String, String>,
        merchantUsername: String
    ): Result<Location>

    suspend fun searchShops(
        query: String,
        claimed: Boolean
    ): Result<List<Shop>>

    suspend fun getShopById(
        id: String
    ): Result<Shop>
}