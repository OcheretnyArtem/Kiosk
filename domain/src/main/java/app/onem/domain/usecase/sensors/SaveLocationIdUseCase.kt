package app.onem.domain.usecase.sensors

import app.onem.domain.base.BaseUseCase
import app.onem.domain.repositories.DataRepository
import com.stripe.stripeterminal.external.models.Location

class SaveLocationIdUseCase(
    private val dataRepository: DataRepository
) : BaseUseCase<Location, Unit> {

    override suspend fun invoke(params: Location?) {
        params?.let {
            dataRepository.saveLocationId(it.id)
        }
    }
}