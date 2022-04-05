package app.onem.domain.usecase.api

import app.onem.domain.base.BaseUseCase
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.usecase.model.CreatePaymentIntentModel
import com.stripe.stripeterminal.external.models.PaymentIntent
import app.onem.domain.core.result.Result

class CreatePaymentIntentUseCase(
    private val apiRepository: KioskRepository
) : BaseUseCase<CreatePaymentIntentModel, Result<PaymentIntent>> {

    override suspend fun invoke(params: CreatePaymentIntentModel?): Result<PaymentIntent> {
        return apiRepository.createPaymentIntent(
            params!!.merchantUsername,
            params.amount,
            params.currency,
            params.paymentMethodTypes,
            params.captureMethod
        )
    }
}