package app.onem.kioskandroid.base

import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import app.onem.kioskandroid.base.permissions.PermissionStatesByResources
import app.onem.kioskandroid.base.permissions.RequestPermissionsContractAdapter
import app.onem.kioskandroid.base.permissions.Resource
import app.onem.kioskandroid.feature.webview.WebViewFragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class BaseFragment(layout: Int) : Fragment(layout) {

    override fun onStart() {
        super.onStart()
        activity?.window?.setSoftInputMode(
            if (this is WebViewFragment) {
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            } else {
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            }
        )
    }

    internal fun navigate(directions: NavDirections) =
        view?.let {
            Navigation.findNavController(it).navigate(directions)
        }

    inline fun <reified R : ActivityResultLauncher<List<Resource>>> Fragment.requestPermissions(
        resources: List<Resource>,
        noinline onResult: (PermissionStatesByResources) -> Unit
    ): ReadOnlyProperty<Fragment, R> = PermissionResultDelegate(this, resources, onResult)

    class PermissionResultDelegate<R : ActivityResultLauncher<List<Resource>>>(
        private val fragment: Fragment,
        private val resources: List<Resource>,
        private val onResult: (PermissionStatesByResources) -> Unit
    ) : ReadOnlyProperty<Fragment, R> {

        private var permissionResult: ActivityResultLauncher<List<Resource>>? = null

        init {
            fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {

                override fun onCreate(owner: LifecycleOwner) {
                    super.onCreate(owner)

                    permissionResult = fragment.registerForActivityResult(
                        RequestPermissionsContractAdapter(
                            ActivityResultContracts.RequestMultiplePermissions(),
                            fragment,
                            resources
                        ),
                        onResult
                    )
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    permissionResult = null
                }
            })
        }

        override fun getValue(thisRef: Fragment, property: KProperty<*>): R {
            permissionResult?.let { return (it as R) }

            error("Failed to Initialize Permission")
        }
    }
}
