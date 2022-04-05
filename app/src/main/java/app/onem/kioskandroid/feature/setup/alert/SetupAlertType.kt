package app.onem.kioskandroid.feature.setup.alert

import androidx.annotation.StringRes
import app.onem.kioskandroid.R

enum class SetupAlertType(
    @StringRes val messageId: Int,
    @StringRes val buttonTextId: Int?
) {

    LOCATION(
        messageId = R.string.locationAlert,
        buttonTextId = null
    ),

    BLUETOOTH(
        messageId = R.string.bluetoothAlert,
        buttonTextId = R.string.bluetoothAlertMessage
    ),

    INTERNET(
        messageId = R.string.internetAlert,
        buttonTextId = R.string.internetAlertMessage
    ),

    LOCATION_PERMISSION(
        messageId = R.string.locationPermissionAlert,
        buttonTextId = R.string.permissionAlertMessage
    ),

    BLUETOOTH_PERMISSION(
        messageId = R.string.bluetoothPermissionAlert,
        buttonTextId = R.string.permissionAlertMessage
    );

    val tag: String
        get() = name
}
