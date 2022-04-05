package app.onem.kioskandroid.feature.setup.reader.location

import app.onem.kioskandroid.base.BaseViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.LocationListCallback
import com.stripe.stripeterminal.external.models.ListLocationsParameters
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.TerminalException
import timber.log.Timber

class LocationViewModel : BaseViewModel<LocationDialogViewState, LocationDialogAction>() {

    override fun initialViewState() = LocationDialogViewState()

    internal fun fetchLocations() {
        val locationParams = ListLocationsParameters.Builder().apply {
            limit = 100
        }.build()

        reduceViewState { it.copy(isFetching = true) }
        Terminal.getInstance().listLocations(locationParams, object : LocationListCallback {
            override fun onFailure(e: TerminalException) {
                Timber.d(e)
            }

            override fun onSuccess(locations: List<Location>, hasMore: Boolean) {
                reduceViewState {
                    it.copy(
                        isFetching = false,
                        locations = locations
                    )
                }
            }
        })
    }

    internal fun locationSelected(location: Location) {
        sendAction(LocationDialogAction.LocationSelected(location))
    }
}