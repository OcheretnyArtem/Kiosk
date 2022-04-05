package app.onem.domain.usecase.shop

import app.onem.domain.base.BaseUseCase
import app.onem.domain.repositories.DataRepository
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.utils.Shop

class SaveSelectedShopUseCase(
    private val kioskRepository: KioskRepository,
    private val dataRepository: DataRepository
) : BaseUseCase<Shop, Unit> {

    override suspend fun invoke(params: Shop?) {
        kioskRepository.saveSelectedShop(params)
        dataRepository.saveSelectedShopId(params?.id)
    }
}