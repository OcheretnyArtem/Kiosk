package app.onem.domain.usecase.api

import app.onem.domain.base.BaseUseCase
import app.onem.domain.core.result.Result
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.usecase.model.CreateLocationModel
import com.stripe.stripeterminal.external.models.Location

class CreateLocationUseCase(
    private val apiRepository: KioskRepository
) : BaseUseCase<CreateLocationModel, Result<Location>> {
    override suspend fun invoke(params: CreateLocationModel?): Result<Location> {
        return apiRepository.createLocation(
            params!!.displayName,
            params.address,
            params.merchantUsername
        )
    }
}