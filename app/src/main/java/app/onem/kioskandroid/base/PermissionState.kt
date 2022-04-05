package app.onem.kioskandroid.base

enum class PermissionState {

    GRANTED,
    DENIED,
    EXPLAINED;

    companion object {

        fun isGranted(state: PermissionState): Boolean =
            state == GRANTED

        fun allGranted(states: Array<PermissionState>): Boolean =
            states.all(::isGranted)
    }
}
