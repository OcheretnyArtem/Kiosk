package app.onem.kioskandroid.base.permissions

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import app.onem.kioskandroid.base.PermissionState

class RequestPermissionsContractAdapter(
    private val requestPermissionsContract: ActivityResultContracts.RequestMultiplePermissions,
    private val fragment: Fragment,
    private val resources: List<Resource>
) : ActivityResultContract<List<Resource>, PermissionStatesByResources>() {

    override fun createIntent(
        context: Context,
        input: List<Resource>
    ): Intent =
        requestPermissionsContract
            .createIntent(
                context,
                input.let(::convertInput)
            )

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): PermissionStatesByResources =
        requestPermissionsContract
            .parseResult(
                resultCode,
                intent
            )
            .let(::convertOutput)

    override fun getSynchronousResult(
        context: Context,
        input: List<Resource>?
    ): SynchronousResult<PermissionStatesByResources>? =
        requestPermissionsContract
            .getSynchronousResult(
                context,
                input?.let(::convertInput)
            )
            ?.map(::convertOutput)

    private fun convertInput(resources: List<Resource>): Array<String> =
        resources
            .flatMap(Resource::getRequiredPermissionsNames)
            .toTypedArray()

    private fun convertOutput(resultMap: Map<String, Boolean>): PermissionStatesByResources =
        resultMap
            .mapValues { (permission, isGranted) ->
                when {
                    isGranted -> {
                        PermissionState.GRANTED
                    }
                    fragment.shouldShowRequestPermissionRationale(permission) -> {
                        PermissionState.DENIED
                    }
                    else -> {
                        PermissionState.EXPLAINED
                    }
                }
            }
            .let(::PermissionStatesByNames)
            .groupByResources(resources)

    private fun <T, R> SynchronousResult<T>.map(transform: (T) -> R): SynchronousResult<R> =
        value
            .let(transform)
            .let(::SynchronousResult)
}
