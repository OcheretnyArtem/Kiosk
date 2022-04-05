package app.onem.kioskandroid.feature.setup.alert

sealed interface SetupAlertAction {

    data class ButtonClicked(
        val alertType: SetupAlertType
    ) : SetupAlertAction
}
