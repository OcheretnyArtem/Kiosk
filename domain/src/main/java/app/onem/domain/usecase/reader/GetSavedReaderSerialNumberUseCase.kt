package app.onem.domain.usecase.reader

import app.onem.domain.base.BaseUseCase
import app.onem.domain.repositories.DataRepository
import app.onem.domain.repositories.KioskRepository
import com.stripe.stripeterminal.external.models.Reader
import kotlinx.coroutines.flow.Flow

class GetSavedReaderSerialNumberUseCase(
    private val dataRepository: DataRepository
) : BaseUseCase<Unit, Flow<String?>> {

    override suspend fun invoke(params: Unit?): Flow<String?> =
        dataRepository.getSelectedReaderSerialNumber()
}