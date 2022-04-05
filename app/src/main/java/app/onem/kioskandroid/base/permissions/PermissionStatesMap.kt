package app.onem.kioskandroid.base.permissions

import app.onem.kioskandroid.base.PermissionState

data class PermissionStatesByResources(
    private val map: Map<Resource, PermissionState>
) : Map<Resource, PermissionState> by map

data class PermissionStatesByNames(
    private val map: Map<String, PermissionState>
) : Map<String, PermissionState> by map {

    fun groupByResources(resources: List<Resource>): PermissionStatesByResources =
        resources
            .associateWith { resource ->
                getStateFor(resource.requiredPermission)
            }
            .let(::PermissionStatesByResources)

    private fun getStateFor(permission: Permission): PermissionState =
        when (permission) {
            is Permission.Single -> {
                val state = permission.name.let(::getValue)
                state
            }
            is Permission.Group -> {
                val states = permission.names.map(::getValue)
                val groupState = states.first()
                require(states.all { it == groupState })
                groupState
            }
        }
}
