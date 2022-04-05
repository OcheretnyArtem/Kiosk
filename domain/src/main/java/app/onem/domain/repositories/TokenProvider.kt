package app.onem.domain.repositories

import app.onem.domain.core.result.Result
import app.onem.domain.core.result.asSuccess
import app.onem.domain.core.result.isSuccess
import app.onem.domain.usecase.api.GetTokenUseCase
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class TokenProvider(
    private val getTokenUseCase: GetTokenUseCase
) : ConnectionTokenProvider, CoroutineScope {

    var name: String = ""

    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
        launch {
            var result: Result<String>? = null
            while (result !is Result.Success) {
                result = getTokenUseCase(name)
                if (result.isSuccess()) {
                    callback.onSuccess(result.asSuccess().value)
                }
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

}