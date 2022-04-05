package app.onem.kioskandroid.utils

import com.stripe.stripeterminal.external.models.Reader

internal fun Reader.getCroppedSerial(): String =
    serialNumber?.split(":")?.get(0) ?: "None"