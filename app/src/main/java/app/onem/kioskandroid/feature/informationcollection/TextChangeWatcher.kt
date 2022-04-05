package app.onem.kioskandroid.feature.informationcollection

import android.text.Editable
import android.text.TextWatcher

class TextChangeWatcher(private val onTextChanged: (newValue: String) -> Unit) : TextWatcher {

    override fun beforeTextChanged(
        charSequence: CharSequence,
        start: Int,
        count: Int,
        after: Int
    ) {
    }

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable) {
        onTextChanged(editable.toString())
    }
}