package app.onem.kioskandroid.utils

fun Boolean.Companion.allTrue(values: Array<Boolean>): Boolean =
    values.all { value -> value == true }
