package app.onem.kioskandroid.utils

import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.showKeyboard() {
    val imm =
        context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
}

fun View.hideKeyboard() {
    val imm =
        context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}
