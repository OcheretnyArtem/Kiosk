package app.onem.kioskandroid.utils

import android.content.Context
import app.onem.kioskandroid.BuildConfig
import java.io.File

class WebAppTools {
    companion object {
        const val webAppName = BuildConfig.WEB_APP
    }
}

private val appPackage = File.separator + WebAppTools.webAppName

val Context.webAppDirFile: File get() = filesDir

val Context.webAppDirPath: String get() = webAppDirFile.absolutePath + appPackage

val Context.webAppIndexPath: String get() = webAppDirPath + File.separator + "index.html"

val Context.webAppIndexFile: File get() = File(webAppIndexPath)
