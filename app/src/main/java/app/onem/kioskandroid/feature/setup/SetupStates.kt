package app.onem.kioskandroid.feature.setup

import app.onem.domain.utils.Shop
import app.onem.kioskandroid.base.Action
import app.onem.kioskandroid.base.ViewState
import app.onem.kioskandroid.feature.setup.alert.SetupAlertType
import com.stripe.stripeterminal.external.models.Reader

data class SetupViewState(
    val isLoading: Boolean = false,
    val shop: Shop? = null,
    val reader: Reader? = null
) : ViewState

sealed class SetupAction : Action {
    object ShowChooseShopAction : SetupAction()
    object ShowTerminalAction : SetupAction()
    data class InitTerminal(val name: String) : SetupAction()
    object AllChosen : SetupAction()
    object Reconnect : SetupAction()
    data class ShowReaderInfoDialog(val reader:Reader) : SetupAction()

    sealed class AlertDialogAction : SetupAction() {
        data class Show(val alertType: SetupAlertType) :  AlertDialogAction()
        data class Hide(val alertType: SetupAlertType) :  AlertDialogAction()
    }
}
