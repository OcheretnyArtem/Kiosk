package app.onem.kioskandroid.base.permissions

import android.Manifest

sealed interface Permission {

    data class Single(val name: String) : Permission

    /**
     * Represents tied permissions that can not be granted/denied separately
     * e.g. [Manifest.permission.BLUETOOTH_SCAN] and [Manifest.permission.BLUETOOTH_CONNECT]
     **/
    data class Group(val names: List<String>) : Permission
}
