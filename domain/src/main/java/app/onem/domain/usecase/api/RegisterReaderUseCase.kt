package app.onem.domain.usecase.api

import app.onem.domain.base.BaseUseCase
import app.onem.domain.core.result.Result
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.usecase.model.RegisterReaderModel

class RegisterReaderUseCase(
    private val apiRepository: KioskRepository
) : BaseUseCase<RegisterReaderModel, Result<Map<String, Any>>> {

    override suspend fun invoke(params: RegisterReaderModel?): Result<Map<String, Any>> {
        return apiRepository.registerReader(
            params!!.label,
            params.registrationCode,
            params.location,
            params.merchantUsername
        )
    }
}