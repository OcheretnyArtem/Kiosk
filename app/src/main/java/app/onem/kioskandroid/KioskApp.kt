package app.onem.kioskandroid

import android.app.Application
import app.onem.data.di.repositoriesModule
import app.onem.data.di.retrofitModule
import app.onem.domain.di.useCasesModule
import app.onem.kioskandroid.di.koinModule
import app.onem.kioskandroid.utils.DebugTools
import com.stripe.stripeterminal.TerminalApplicationDelegate
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KioskApp : Application() {

    override fun onCreate() {
        super.onCreate()

        TerminalApplicationDelegate.onCreate(this)
        DebugTools.init(this)

        startKoin {
//            androidLogger(Level.DEBUG)
            androidContext(this@KioskApp)
            modules(
                listOf(
                    repositoriesModule,
                    koinModule,
                    useCasesModule,
                    retrofitModule
                )
            )
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        TerminalApplicationDelegate.onTrimMemory(this, level)
    }
}
