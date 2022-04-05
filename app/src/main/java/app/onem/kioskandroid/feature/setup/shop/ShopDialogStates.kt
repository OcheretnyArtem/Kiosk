package app.onem.kioskandroid.feature.setup.shop

import app.onem.domain.utils.Shop
import app.onem.kioskandroid.base.Action
import app.onem.kioskandroid.base.ViewState

data class ShopDialogViewState(
    val shopsList: List<Shop> = emptyList(),
    val error: ShopDialogError? = null,
    val isLoading: Boolean = false
) : ViewState

sealed class ShopDialogAction : Action {

    data class HideDialog(
        val selectedShop: Shop
    ) : ShopDialogAction()
}
