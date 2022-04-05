package app.onem.kioskandroid.server

import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.CallLogging
import io.ktor.gson.gson
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val GRACE_PERIOD_MILLIS = 1000L
private const val TIMEOUT_MILLIS = 2000L

class WebServer(localAddr: String, port: Int, wwwroot: File) {

    private val server = embeddedServer(Netty, host = localAddr, port = port) {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
                disableHtmlEscaping()
            }
        }
        install(CallLogging)
        routing {
            static("#") {
                files(wwwroot)
            }
        }
    }

    suspend fun start() {
        withContext(Dispatchers.IO) {
            server.start()
        }
    }

    fun stop() {
        server.stop(GRACE_PERIOD_MILLIS, TIMEOUT_MILLIS)
    }

}