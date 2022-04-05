package app.onem.kioskandroid.feature.webview

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.WebView
import app.onem.kioskandroid.utils.hideKeyboard

class KioskWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {

        val connection = BaseInputConnection(this, false)
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE
        return connection
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val dispatchFirst = super.dispatchKeyEvent(event)
        if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
            hideKeyboard()
        }
        return dispatchFirst
    }
}