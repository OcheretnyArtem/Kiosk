package app.onem.domain.usecase.api

import app.onem.domain.base.BaseUseCase
import app.onem.domain.core.result.Result
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.usecase.model.CapturePaymentIntentModel
import app.onem.domain.usecase.model.IntentSecretModel

class CapturePaymentIntentUseCase(
    private val apiRepository: KioskRepository
) : BaseUseCase<CapturePaymentIntentModel, Result<IntentSecretModel>> {
    override suspend fun invoke(params: CapturePaymentIntentModel?): Result<IntentSecretModel> {
        return apiRepository.capturePaymentIntent(params!!.merchantUsername, params.paymentIntentId)
    }
}