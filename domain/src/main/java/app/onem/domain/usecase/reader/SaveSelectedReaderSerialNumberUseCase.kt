package app.onem.domain.usecase.reader

import app.onem.domain.base.BaseUseCase
import app.onem.domain.repositories.DataRepository
import app.onem.domain.repositories.KioskRepository
import com.stripe.stripeterminal.external.models.Reader

class SaveSelectedReaderSerialNumberUseCase(
    private val dataRepository: DataRepository
) : BaseUseCase<Reader, Unit> {

    override suspend fun invoke(params: Reader?) {
        dataRepository.saveSelectedReaderSerialNumber(params?.serialNumber)
    }
}