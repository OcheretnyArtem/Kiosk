package app.onem.domain.usecase.api

import app.onem.domain.base.BaseUseCase
import app.onem.domain.core.result.Result
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.usecase.model.RetrievePaymentIntentModel
import com.stripe.stripeterminal.external.models.PaymentIntent

class RetrievePaymentIntentUseCase(
    private val apiRepository: KioskRepository
) : BaseUseCase<RetrievePaymentIntentModel, Result<PaymentIntent>> {
    override suspend fun invoke(params: RetrievePaymentIntentModel?): Result<PaymentIntent> {
        return apiRepository.retrievePaymentIntent(
            params!!.merchantUsername,
            params.paymentIntentId
        )
    }
}