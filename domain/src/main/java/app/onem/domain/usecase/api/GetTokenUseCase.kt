package app.onem.domain.usecase.api

import app.onem.domain.base.BaseUseCase
import app.onem.domain.core.result.Result
import app.onem.domain.repositories.KioskRepository

class GetTokenUseCase(
    private val apiRepository: KioskRepository
) : BaseUseCase<String, Result<String>> {

    override suspend fun invoke(params: String?): Result<String> {
        return apiRepository.getConnectionToken(params!!)
    }
}
