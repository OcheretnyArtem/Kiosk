package app.onem.kioskandroid.feature.setup.reader.reconnect

import app.onem.kioskandroid.base.Action
import app.onem.kioskandroid.base.ViewState
import com.stripe.stripeterminal.external.models.Reader

data class ReconnectViewState(
    val isConnecting: Boolean = true,
    val reader: Reader? = null
) : ViewState

sealed class ReconnectAction : Action {
    object Close : ReconnectAction()
}