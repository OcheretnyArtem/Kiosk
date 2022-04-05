package app.onem.kioskandroid.feature.setup.reader.reconnect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import app.onem.kioskandroid.R
import app.onem.kioskandroid.databinding.DReconnectBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.stripe.stripeterminal.external.models.Reader
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.viewModel

class ReconnectDialog(private val onReaderReconnected: ((Reader?) -> Unit)? = null) :
    DialogFragment(R.layout.d_reconnect) {

    companion object {
        private const val ALPHA_VISIBLE = 1f
        private const val ALPHA_HIDDEN = 0f
        private const val ANIMATION_DURATION_MS = 200L
    }

    private val viewModel by viewModel<ReconnectViewModel>()
    private val binding by viewBinding<DReconnectBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onReaderReconnected = onReaderReconnected
        lifecycleScope.launchWhenStarted {
            viewModel.viewStateFlow.collect(::onStateUpdated)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.actionFlow.collect(::handleAction)
        }
        isCancelable = false
    }

    private fun onStateUpdated(state: ReconnectViewState) {
        with(binding) {
            if (state.isConnecting) {
                loadingImage
                    .animate()
                    .alpha(ALPHA_HIDDEN)
                    .duration = ANIMATION_DURATION_MS
                progressAnimation
                    .animate()
                    .alpha(ALPHA_VISIBLE)
                    .duration = ANIMATION_DURATION_MS
                progressFilled.visibility = View.INVISIBLE
            } else {
                messageTextView.text =
                    getString(if (state.reader != null) R.string.reconnectionSucceeded else R.string.reconnectionFailed)
                loadingImage.setImageResource(
                    if (state.reader != null) {
                        R.drawable.ic_success
                    } else {
                        R.drawable.ic_fail
                    }
                )
                loadingImage
                    .animate()
                    .alpha(ALPHA_VISIBLE)
                    .duration = ANIMATION_DURATION_MS
                progressAnimation
                    .animate()
                    .alpha(ALPHA_HIDDEN)
                    .duration = ANIMATION_DURATION_MS
                progressFilled.visibility = View.VISIBLE
            }
        }
    }

    private fun handleAction(action: ReconnectAction) {
        if (action is ReconnectAction.Close) {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.reconnectToReader()
    }
}
