package app.onem.kioskandroid.utils

import android.os.Build

class EmulatorTools {

    companion object {
        fun isEmulator(): Boolean {
            return (Build.MANUFACTURER.contains("Genymotion")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.lowercase().contains("droid4x")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.HARDWARE == "goldfish"
                    || Build.HARDWARE == "vbox86"
                    || Build.HARDWARE.lowercase().contains("nox")
                    || Build.FINGERPRINT.startsWith("generic")
                    || Build.PRODUCT == "sdk"
                    || Build.PRODUCT == "google_sdk"
                    || Build.PRODUCT == "sdk_x86"
                    || Build.PRODUCT == "vbox86p"
                    || Build.PRODUCT.lowercase().contains("nox")
                    || Build.BOARD.lowercase().contains("nox")
                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")))
        }
    }
}
