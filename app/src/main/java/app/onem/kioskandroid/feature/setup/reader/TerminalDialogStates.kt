package app.onem.kioskandroid.feature.setup.reader

import app.onem.kioskandroid.base.Action
import app.onem.kioskandroid.base.ViewState
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader

data class TerminalDialogViewState(
    var isEmulator: Boolean = false,
    var location: Location? = null,
    var readersList: List<Reader> = emptyList()
) : ViewState

sealed class TerminalDialogAction : Action {
    object HideDialog : TerminalDialogAction()
    object ShowLocationDialog : TerminalDialogAction()
    object NoLocationForReader : TerminalDialogAction()
}
