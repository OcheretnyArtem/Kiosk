package app.onem.kioskandroid.server

import android.content.Context
import app.onem.kioskandroid.utils.WebAppTools
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.CallLogging
import io.ktor.gson.gson
import io.ktor.http.content.*
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext

private const val GRACE_PERIOD_MILLIS = 1000L
private const val TIMEOUT_MILLIS = 2000L
private const val LOCAL_HOST = "localhost"
private const val PORT = 3000

class WebServer(val context: Context, val coroutineContext: CoroutineContext) {

    private val appName = WebAppTools.webAppName
    private val rootFileFolder = context.filesDir

    init {
        File(context.filesDir.path, appName).mkdir()
        copyWebResources(context, appName)
    }

    private val server = embeddedServer(Netty, host = LOCAL_HOST, port = PORT) {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
                disableHtmlEscaping()
            }
        }
        install(CallLogging)
        routing {
            static("/") {
                files(rootFileFolder)
                default("${rootFileFolder}/index.html")
            }
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

    private fun copyFile(context: Context, filePath: String) {
        val file = context.assets.open(filePath)
        val outFile = File(rootFileFolder, filePath.removePrefix(appName))

        val outStream = FileOutputStream(outFile)

        file.copyTo(outStream)
        outStream.close()
    }

    private fun copyWebResources(context: Context, path: String) {
        val assets = context.assets
        val asset = assets.list(path)

        asset?.forEach { list ->
            val listPath = "$path/$list"
            if (list.toString().contains("apple") || list.toString().contains(".")) {
                copyFile(context, listPath)
            } else {

                File(rootFileFolder.path, listPath.removePrefix(appName)).mkdir()
                copyWebResources(context, listPath)
            }
        }
    }
}