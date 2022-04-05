package app.onem.kioskandroid.feature.setup.shop

import android.content.Context
import app.onem.kioskandroid.R

sealed interface ShopDialogError {

    fun toLocalizedString(context: Context): String

    data class NoResultsFor(
        val query: String
    ) : ShopDialogError {

        override fun toLocalizedString(context: Context): String =
            if (query.isEmpty()) {
                context.getString(R.string.no_results_found)
            } else {
                context.getString(R.string.no_results_found_for, query)
            }
    }
}