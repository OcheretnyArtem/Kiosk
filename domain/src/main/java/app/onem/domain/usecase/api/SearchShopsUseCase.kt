package app.onem.domain.usecase.api

import app.onem.domain.base.BaseUseCase
import app.onem.domain.core.result.Result
import app.onem.domain.core.result.checkIsEmpty
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.utils.Shop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchShopsUseCase(
    private val apiRepository: KioskRepository
) : BaseUseCase<String, Flow<SearchShopsUseCase.RequestState>> {

    data class ResultByQuery(
        val query: String,
        val result: Result<List<Shop>>
    )

    sealed interface RequestState {

        object Loading : RequestState

        data class Terminal(
            val resultByQuery: ResultByQuery
        ) : RequestState
    }

    override suspend fun invoke(params: String?): Flow<RequestState> =
        flow {
            emit(RequestState.Loading)
            val resultByQuery = searchShops(params!!)
            emit(RequestState.Terminal(resultByQuery))
        }

    private suspend fun searchShops(query: String): ResultByQuery =
        apiRepository
            .searchShops(
                query = query,
                claimed = true
            )
            .checkIsEmpty()
            .let { result ->
                ResultByQuery(query, result)
            }
}