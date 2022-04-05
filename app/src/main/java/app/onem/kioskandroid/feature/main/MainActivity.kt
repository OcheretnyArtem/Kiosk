package app.onem.kioskandroid.feature.main

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import app.onem.kioskandroid.R
import app.onem.kioskandroid.base.OrderCancelable
import app.onem.kioskandroid.databinding.AcMainBinding
import app.onem.kioskandroid.feature.webview.WebViewFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(R.layout.ac_main) {

    private val viewModel by viewModel<MainViewModel>()
    private val binding: AcMainBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Application supports State Restoration after Configuration Changes
        // but not after Process Death (because user needs to connect to a reader on Setup fragment).
        super.onCreate(
            if (viewModel.canRestoreUIState) savedInstanceState
            else null
        )
        viewModel.canRestoreUIState = true
    }

    override fun onStart() {
        super.onStart()
        observeViewModel()

        viewModel.restartScreensaverListener()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopScreensaverListener()
    }

    override fun onBackPressed() {
        getWebView()?.stopServer()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_UP) {
            viewModel.restartScreensaverListener()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun getWebView(): WebViewFragment? {
        val topFragment = getTopFragment()
        return if (topFragment is WebViewFragment) {
            topFragment
        } else null
    }

    private fun getTopFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenCreated {
            viewModel.viewStateFlow.collect(::setViewState)
        }
        lifecycleScope.launchWhenCreated {
            viewModel.actionFlow.collect(::runAction)
        }
    }

    private fun setViewState(state: MainViewState) {
        with(binding) {
            screensaver.root.isVisible = state.isScreensaverShowing
        }
    }

    private fun runAction(action: MainAction) {
        when (action) {
            MainAction.ReturnToShopScreen -> {
                val topFragment = getTopFragment()
                if (topFragment is OrderCancelable) {
                    topFragment.cancelOrderProcess()
                }
            }
        }
    }
}
