package app.onem.kioskandroid.feature.setup.shop

import app.onem.domain.core.result.Result
import app.onem.domain.usecase.api.SearchShopsUseCase
import app.onem.domain.usecase.api.SearchShopsUseCase.ResultByQuery
import app.onem.domain.usecase.api.SearchShopsUseCase.RequestState
import app.onem.domain.utils.Shop
import app.onem.kioskandroid.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChooseShopViewModel(
    private val searchShopsUseCase: SearchShopsUseCase
) : BaseViewModel<ShopDialogViewState, ShopDialogAction>() {

    companion object {
        private const val TYPING_TIMEOUT_MILLIS = 500L
    }

    override fun initialViewState() = ShopDialogViewState()

    private val _query: MutableStateFlow<String> = MutableStateFlow("")

    init {
        _query
            .debounce(this::getTypingTimeout)
            .flatMapLatest(searchShopsUseCase::invoke)
            .onEach(this::dispatchShopDialogViewState)
            .launchIn(this)
    }

    private fun getTypingTimeout(query: String): Long =
        when {
            query.isEmpty() -> 0
            else -> TYPING_TIMEOUT_MILLIS
        }

    private fun dispatchShopDialogViewState(requestState: RequestState) {
        when (requestState) {
            RequestState.Loading -> showLoading()
            is RequestState.Terminal -> showResult(requestState.resultByQuery)
        }
    }

    private fun showResult(resultByQuery: ResultByQuery) {
        val result = resultByQuery.result
        val query = resultByQuery.query
        when (result) {
            is Result.Failure<*>,
            is Result.Success.Empty -> {
                showError(ShopDialogError.NoResultsFor(query))
            }
            is Result.Success -> {
                showShopsList(result.value)
            }
        }
    }

    private fun showError(error: ShopDialogError) {
        reduceViewState {
            it.copy(
                error = error,
                isLoading = false
            )
        }
    }

    private fun showShopsList(shopsList: List<Shop>) {
        reduceViewState {
            it.copy(
                shopsList = shopsList,
                isLoading = false
            )
        }
    }

    private fun showLoading() {
        reduceViewState {
            it.copy(
                isLoading = true,
                error = null,
                shopsList = emptyList()
            )
        }
    }

    fun onShopClicked(shop: Shop) {
        launch {
            sendAction(ShopDialogAction.HideDialog(shop))
        }
    }

    fun onQueryTextChanged(newQuery: CharSequence) {
        launch {
            _query.emit(newQuery.toString())
        }
    }
}