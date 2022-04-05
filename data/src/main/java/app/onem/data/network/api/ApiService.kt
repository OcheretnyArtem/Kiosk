package app.onem.data.network.api

import app.onem.data.model.MerchantModel
import app.onem.data.network.request.*
import app.onem.data.network.response.ConnectionToken
import app.onem.data.network.response.IntentResponse
import app.onem.domain.core.result.Result
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.PaymentIntent
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("merchants/stripe/connection-token")
    suspend fun getConnectionToken(@Body param: UserNameRequest): Result<ConnectionToken>

    @POST("merchants/stripe/create-payment-intent")
    suspend fun createPaymentIntent(@Body param: CreatePaymentIntentRequest): Result<PaymentIntent>

    @GET("merchants/stripe/retrieve-payment-intent")
    suspend fun retrievePaymentIntent(
        @Query("merchantUsername") merchantUsername: String,
        @Query("payment_intent_id") paymentIntentId: String
    ): Result<PaymentIntent>

    @POST("merchants/stripe/capture-payment-intent")
    suspend fun capturePaymentIntent(@Body param: CapturePaymentIntentRequest): Result<IntentResponse>

    @POST("merchants/stripe/register-reader")
    suspend fun registerReader(@Body param: RegisterReaderRequest): Result<Map<String, Any>>

    @POST("merchants/stripe/create-location")
    suspend fun createLocation(@Body param: CreateLocationRequest): Result<Location>

    @GET("merchants/search")
    suspend fun searchMerchants(
        @Query("name") name: String,
        @Query("claimed") claimed: Boolean
    ): Result<List<MerchantModel>>

    @GET("merchants")
    suspend fun getMerchantById(
        @Query("id") id: String
    ): Result<MerchantModel>
}
