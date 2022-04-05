package app.onem.kioskandroid.feature.setup.reader.create_location

import app.onem.kioskandroid.base.Action
import app.onem.kioskandroid.base.ViewState


data class CreateLocationDialogViewStates(
    val error: LocationEmptyFieldType? = null,
    val isProgressVisible: Boolean = false
) : ViewState

sealed class CreateLocationDialogAction : Action {
    object CloseDialogWithSuccess : CreateLocationDialogAction()
}
