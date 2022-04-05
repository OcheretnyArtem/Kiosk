package app.onem.kioskandroid.feature.main

import app.onem.kioskandroid.base.Action
import app.onem.kioskandroid.base.ViewState

data class MainViewState(
    val isScreensaverShowing: Boolean = false
) : ViewState

sealed class MainAction : Action {
    object ReturnToShopScreen : MainAction()
}