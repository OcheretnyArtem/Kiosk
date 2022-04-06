package app.onem.kioskandroid.server

import android.content.Context
import app.onem.kioskandroid.utils.webAppDirFile
import app.onem.kioskandroid.utils.webAppDirPath
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

private const val GRACE_PERIOD_MILLIS = 1000L
private const val TIMEOUT_MILLIS = 2000L
private const val LOCAL_HOST = "localhost"
private const val PORT = 3000

class WebServer(val cotnext: Context, val coroutineContext: CoroutineContext) {

    private val wwwroot = cotnext.webAppDirFile.canonicalFile

    private val server = embeddedServer(Netty, host = LOCAL_HOST, port = PORT) {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
                disableHtmlEscaping()
            }
        }
        install(CallLogging)
        routing {
            static() {
                files(wwwroot)
            }
            default("index.html")
        }
    }

    suspend fun start() {
        withContext(coroutineContext) {
            server.start()
        }
    }

    fun stop() {
        server.stop(GRACE_PERIOD_MILLIS, TIMEOUT_MILLIS)
    }

}