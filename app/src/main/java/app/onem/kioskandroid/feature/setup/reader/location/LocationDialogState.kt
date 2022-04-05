package app.onem.kioskandroid.feature.setup.reader.location

import app.onem.kioskandroid.base.Action
import app.onem.kioskandroid.base.ViewState
import com.stripe.stripeterminal.external.models.Location

data class LocationDialogViewState(
    var isFetching: Boolean = true,
    var locations: List<Location> = emptyList()
) : ViewState

sealed class LocationDialogAction : Action {
    data class LocationSelected(val location: Location) : LocationDialogAction()
}
