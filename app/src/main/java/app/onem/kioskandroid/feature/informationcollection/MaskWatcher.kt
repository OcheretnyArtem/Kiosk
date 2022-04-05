package app.onem.kioskandroid.feature.informationcollection

import android.text.Editable
import android.text.TextWatcher

class MaskWatcher(val mask: String, val maskSymbol: Char = '#') : TextWatcher {

    companion object {
        fun usPhoneWatcher(): MaskWatcher {
            return MaskWatcher("+#(###)###-####")
        }
    }

    private var isRunning = false
    private var isDeleting = false

    override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
        isDeleting = count > after
    }

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable) {
        if (isRunning || isDeleting) {
            return
        }
        isRunning = true
        val editableLength = editable.length
        if (editableLength < mask.length) {
            if (mask[editableLength] != maskSymbol) {
                editable.append(mask[editableLength])
            } else if (mask[editableLength - 1] != maskSymbol) {
                editable.insert(editableLength - 1, mask, editableLength - 1, editableLength)
            }
        }
        isRunning = false
    }
}