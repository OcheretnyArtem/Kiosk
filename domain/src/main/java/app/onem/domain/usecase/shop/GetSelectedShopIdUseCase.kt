package app.onem.domain.usecase.shop

import app.onem.domain.base.BaseUseCase
import app.onem.domain.repositories.DataRepository
import kotlinx.coroutines.flow.Flow

class GetSelectedShopIdUseCase(
    private val dataRepository: DataRepository
) : BaseUseCase<Unit, Flow<String?>> {

    override suspend fun invoke(params: Unit?): Flow<String?> {
        return dataRepository.getSelectedShopId()
    }
}
