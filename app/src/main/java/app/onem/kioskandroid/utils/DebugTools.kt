package app.onem.kioskandroid.utils

import app.onem.kioskandroid.KioskApp
import timber.log.Timber

object DebugTools {
    private const val APP_TAG = "KioskApp"

    fun init(app: KioskApp) {
        Timber.plant(DebugTree(APP_TAG))
    }
}

class DebugTree(private val customTag: String) : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, customTag, message, t)
    }
}
