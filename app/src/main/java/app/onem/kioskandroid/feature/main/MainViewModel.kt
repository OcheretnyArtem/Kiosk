package app.onem.kioskandroid.feature.main

import app.onem.kioskandroid.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel : BaseViewModel<MainViewState, MainAction>() {

    companion object {
        private val SCREENSAVER_COUNTDOWN: Long = TimeUnit.MINUTES.toMillis(5L)
    }

    private var countdownJob: Job? = null
    override fun initialViewState(): MainViewState = MainViewState()

    var canRestoreUIState: Boolean = false

    internal fun restartScreensaverListener() {
        setScreensaverVisible(isVisible = false)
        countdownJob?.cancel()
        countdownJob = launch {
            delay(SCREENSAVER_COUNTDOWN)
            sendAction(MainAction.ReturnToShopScreen)
            setScreensaverVisible(isVisible = true)
        }
    }

    internal fun stopScreensaverListener() {
        countdownJob?.cancel()
        countdownJob = null
    }

    private fun setScreensaverVisible(isVisible: Boolean) {
        reduceViewState { it.copy(isScreensaverShowing = isVisible) }
    }
}
