package app.onem.kioskandroid.utils

import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import timber.log.Timber

class CopyUtils {
    fun copyFileOrDir(assetManager: AssetManager, path: String, webDir: String) {
        try {
            val assets = assetManager.list(path)
            if (assets!!.isEmpty()) {
                copyFile(assetManager, path, webDir)
            } else {
                val fullPath = "$webDir/$path"
                val dir = File(fullPath)
                if (!dir.exists()) dir.mkdir()
                for (i in assets.indices) {
                    copyFileOrDir(assetManager, path + "/" + assets[i], webDir)
                }
            }
        } catch (ex: IOException) {
            Timber.e(ex, "I/O Exception")
        }
    }

    private fun copyFile(assetManager: AssetManager, filename: String, webDir: String) {
        try {
            val inputStream = assetManager.open(filename)
            val newFileName = "$webDir/$filename"
            val outputStream = FileOutputStream(newFileName)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            inputStream.close()
            outputStream.flush()
            outputStream.close()
        } catch (e: java.lang.Exception) {
            Timber.d(e)
        }
    }
}