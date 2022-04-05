package app.onem.kioskandroid.feature.setup.reader.create_location

import app.onem.domain.core.result.asFailure
import app.onem.domain.core.result.isSuccess
import app.onem.domain.repositories.KioskRepository
import app.onem.domain.usecase.api.CreateLocationUseCase
import app.onem.domain.usecase.model.CreateLocationModel
import app.onem.kioskandroid.base.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateLocationViewModel(
    private val kioskRepository: KioskRepository,
    private val createLocationUseCase: CreateLocationUseCase
) : BaseViewModel<CreateLocationDialogViewStates, CreateLocationDialogAction>() {

    override fun initialViewState() = CreateLocationDialogViewStates()

    internal fun createLocation(
        displayName: String,
        lineFirst: String,
        lineSecond: String,
        city: String,
        state: String,
        country: String,
        postalCode: String
    ) {
        if (displayName.isEmpty()) {
            reduceViewState { it.copy(error = LocationEmptyFieldType.DisplayName) }
        } else if (lineFirst.isEmpty()) {
            reduceViewState { it.copy(error = LocationEmptyFieldType.LineFirst) }
        } else {
            launch {
                reduceViewState {
                    it.copy(
                        error = null,
                        isProgressVisible = true
                    )
                }
                val params: MutableMap<String, String> = mutableMapOf()
                // TODO ask about accepted countries
                params["country"] = "US"
                if (lineFirst.isNotEmpty()) params["line1"] = lineFirst
                if (lineSecond.isNotEmpty()) params["line2"] = lineSecond
                if (city.isNotEmpty()) params["city"] = city
                if (state.isNotEmpty()) params["state"] = state
                if (postalCode.isNotEmpty()) params["postal_code"] = postalCode

                val createLocationModel = CreateLocationModel(
                    displayName = displayName,
                    address = params,
                    merchantUsername = kioskRepository.selectedShop?.code ?: ""
                )
                val result = createLocationUseCase(createLocationModel)
                reduceViewState {
                    it.copy(isProgressVisible = false)
                }
                if (result.isSuccess()) {
                    sendAction(CreateLocationDialogAction.CloseDialogWithSuccess)
                } else {
                    Timber.d(result.asFailure().error)
                }
            }
        }
    }
}

enum class LocationEmptyFieldType {
    DisplayName,
    LineFirst
}
