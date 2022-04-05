package app.onem.kioskandroid.utils

import android.os.Bundle

fun Bundle.putEnum(key:String, enum: Enum<*>){
    putString(key, enum.name)
}

inline fun <reified T : Enum<T>> Bundle.requireEnum(key: String): T =
    enumValueOf(getString(key)!!)
