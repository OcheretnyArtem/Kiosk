package app.onem.domain.usecase.shop

import app.onem.domain.base.BaseUseCase
import app.onem.domain.core.result.toNullable
import app.onem.domain.repositories.DataRepository
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.utils.Shop
import app.onem.domain.utils.Shops
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSelectedShopUseCase(
    private val dataRepository: DataRepository,
    private val apiRepository: KioskRepository
) : BaseUseCase<Unit, Flow<Shop?>> {

    override suspend fun invoke(params: Unit?): Flow<Shop?> {
        return dataRepository.getSelectedShopId().map {
            if (it == Shops.testShop.code) {
                Shops.testShop
            } else {
                it?.let { id ->
                    apiRepository.getShopById(id).toNullable()
                }
            }
        }
    }
}
