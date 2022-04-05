package app.onem.kioskandroid.base.permissions

import android.Manifest
import android.os.Build

enum class Resource(val requiredPermission: Permission) {

    LOCATION(
        requiredPermission = Permission.Single(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    ),
    BLUETOOTH(
        requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Permission.Group(listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            ))
        } else {
            Permission.Single(
                Manifest.permission.BLUETOOTH
            )
        }
    );

    fun getRequiredPermissionsNames(): List<String> =
        when (requiredPermission) {
            is Permission.Group -> requiredPermission.names
            is Permission.Single -> listOf(requiredPermission.name)
        }
}
