package app.onem.kioskandroid.utils

fun Double?.format(decimalPlaces: Int = 1): String =
    if (this != null) String.format("%.${decimalPlaces}f", this) else ""
