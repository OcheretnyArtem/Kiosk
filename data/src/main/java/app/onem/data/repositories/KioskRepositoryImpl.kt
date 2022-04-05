package app.onem.data.repositories

import app.onem.data.model.MerchantModel
import app.onem.data.model.toShop
import app.onem.data.network.api.ApiService
import app.onem.data.network.request.CapturePaymentIntentRequest
import app.onem.data.network.request.CreateLocationRequest
import app.onem.data.network.request.CreatePaymentIntentRequest
import app.onem.data.network.request.RegisterReaderRequest
import app.onem.data.network.request.UserNameRequest
import app.onem.domain.BuildConfig
import app.onem.domain.core.result.Result
import app.onem.domain.core.result.map
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.usecase.model.IntentSecretModel
import app.onem.domain.utils.Shop
import app.onem.domain.utils.Shops
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.Reader
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

internal class KioskRepositoryImpl(
    private val apiService: ApiService
) : KioskRepository, CoroutineScope {

    override var selectedShop: Shop? = null

    private val _eventChannelReader = Channel<Reader>(Channel.BUFFERED)
    override val selectedReader: Flow<Reader> get() = _eventChannelReader.receiveAsFlow()

    private val _eventChannelShop = MutableSharedFlow<Shop?>()
    override val selectedShopFlow = _eventChannelShop.asSharedFlow() // read-only public view

    override fun saveSelectedShop(shop: Shop?) {
        selectedShop = shop
        launch {
            _eventChannelShop.emit(shop)
        }
    }

    override fun saveSelectedReader(reader: Reader?) {
        reader?.let {
            launch {
                _eventChannelReader.send(it)
            }
        }
    }

    override suspend fun getConnectionToken(name: String): Result<String> =
        withContext(Dispatchers.IO) {
            apiService.getConnectionToken(UserNameRequest(name))
                .map { result -> result.secret }
        }

    override suspend fun createPaymentIntent(
        merchantUsername: String,
        amountInCents: Int,
        currency: String,
        paymentMethodTypes: List<String>,
        captureMethod: String
    ): Result<PaymentIntent> {
        return withContext(Dispatchers.IO) {
            apiService.createPaymentIntent(
                CreatePaymentIntentRequest(
                    merchantUsername = merchantUsername,
                    amount = amountInCents,
                    currency = currency,
                    paymentMethodTypes = paymentMethodTypes,
                    captureMethod = captureMethod
                )
            )
        }
    }

    override suspend fun retrievePaymentIntent(merchantUsername: String, paymentIntentId: String) =
        withContext(Dispatchers.IO) {
            apiService.retrievePaymentIntent(merchantUsername, paymentIntentId)
        }

    override suspend fun capturePaymentIntent(
        merchantUsername: String,
        paymentIntentId: String
    ): Result<IntentSecretModel> =
        withContext(Dispatchers.IO) {
            apiService.capturePaymentIntent(
                CapturePaymentIntentRequest(
                    merchantUsername = merchantUsername,
                    paymentIntentId = paymentIntentId
                )
            ).map { IntentSecretModel(it.intent, it.secret) }
        }

    override suspend fun registerReader(
        label: String,
        registrationCode: String,
        location: String,
        merchantUsername: String
    ) = withContext(Dispatchers.IO) {
        apiService.registerReader(
            RegisterReaderRequest(
                label = label,
                registrationCode = registrationCode,
                location = location,
                merchantUsername = merchantUsername
            )
        )
    }

    override suspend fun createLocation(
        displayName: String,
        address: Map<String, String>,
        merchantUsername: String
    ) = withContext(Dispatchers.IO) {
        apiService.createLocation(
            CreateLocationRequest(
                displayName = displayName,
                address = address,
                merchantUsername = merchantUsername
            )
        )
    }

    override suspend fun searchShops(
        query: String,
        claimed: Boolean
    ): Result<List<Shop>> {
        return withContext(Dispatchers.IO) {
            apiService
                .searchMerchants(
                    name = query,
                    claimed = claimed
                )
                .map { list -> list.map(MerchantModel::toShop) }
                .map { list ->
                    return@map if (BuildConfig.DEBUG) {
                        list
                    } else {
                        list.plus(Shops.testShop)
                    }
                }
        }
    }

    override suspend fun getShopById(
        id: String
    ): Result<Shop> {
        return withContext(Dispatchers.IO) {
            apiService
                .getMerchantById(id)
                .map(MerchantModel::toShop)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob() + defaultCoroutineExceptionHandler

    private val defaultCoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }
}