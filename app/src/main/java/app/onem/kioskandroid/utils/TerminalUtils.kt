package app.onem.kioskandroid.utils

import android.content.Context
import app.onem.domain.repositories.TokenProvider
import app.onem.kioskandroid.BuildConfig
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.*
import com.stripe.stripeterminal.external.models.*
import com.stripe.stripeterminal.log.LogLevel
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object TerminalUtils {

    fun initTerminal(tokenProvider: TokenProvider, context: Context) {
        if (Terminal.isInitialized()) {
            Terminal.getInstance().clearCachedCredentials()
        }
        if (!Terminal.isInitialized()) {
            val listener = object : TerminalListener {
                override fun onUnexpectedReaderDisconnect(reader: Reader) {

                }
            }
            try {
                Terminal.initTerminal(
                    context = context,
                    logLevel = LogLevel.VERBOSE,
                    tokenProvider = tokenProvider,
                    listener = listener
                )
            } catch (exception: TerminalException) {
                Timber.e(exception)
            }
        }
    }

    fun connectToReader(
            reader: Reader,
            location: Location?,
            readerListener: BluetoothReaderListener,
            readerCallback: ReaderCallback? = null,
    ) {
        if (BuildConfig.DEBUG && EmulatorTools.isEmulator()) {
            useTestCard()
        }
        Terminal.getInstance().connectBluetoothReader(
                reader,
                ConnectionConfiguration.BluetoothConnectionConfiguration(
                        location?.id ?: "test location"
                ),
                readerListener,
                readerCallback ?: object : ReaderCallback {
                    override fun onFailure(e: TerminalException) {
                    }

                    override fun onSuccess(reader: Reader) {
                    }
                }
        )
    }

    private fun useTestCard() {
        Terminal.getInstance().simulatorConfiguration = SimulatorConfiguration(
            simulatedCard = SimulatedCard(
                cardType = SimulatedCardType.VISA
            )
        )
    }

    fun disconnectReader(onSuccess: () -> Unit, onFailure: (e: TerminalException) -> Unit) {
        Terminal.getInstance().disconnectReader(object : Callback {
            override fun onSuccess() {
                onSuccess.invoke()
            }

            override fun onFailure(e: TerminalException) {
                onFailure.invoke(e)
            }
        })
    }

    suspend fun findLocationById(locationId: String): Location? {
        val params = ListLocationsParameters.Builder().apply {
            limit = 100
        }.build()
        return suspendCoroutine { continuation ->
            Terminal.getInstance().listLocations(params, object : LocationListCallback {
                override fun onFailure(e: TerminalException) {
                    continuation.resume(null)
                }

                override fun onSuccess(locations: List<Location>, hasMore: Boolean) {
                    continuation.resume(locations.singleOrNull { it.id == locationId })
                }
            })
        }
    }
}
