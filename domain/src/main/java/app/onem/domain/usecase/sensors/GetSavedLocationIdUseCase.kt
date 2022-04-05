package app.onem.domain.usecase.sensors

import app.onem.domain.base.BaseUseCase
import app.onem.domain.repositories.DataRepository
import kotlinx.coroutines.flow.Flow

class GetSavedLocationIdUseCase(
    private val dataRepository: DataRepository
) : BaseUseCase<Unit, Flow<String?>> {

    override suspend fun invoke(params: Unit?): Flow<String?> {
        return dataRepository.getSavedLocationId()
    }
}