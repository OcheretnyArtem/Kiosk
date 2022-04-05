package app.onem.kioskandroid.utils

import android.os.Bundle
import androidx.fragment.app.Fragment

fun <T : Fragment> T.setArgs(actionWithBundle: Bundle.() -> Unit): T =
    apply {
        arguments = Bundle().apply(actionWithBundle)
    }
