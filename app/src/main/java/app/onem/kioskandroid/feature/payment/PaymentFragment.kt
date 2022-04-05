package app.onem.kioskandroid.feature.payment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.onem.kioskandroid.R
import app.onem.kioskandroid.base.BaseFragment
import app.onem.kioskandroid.databinding.FrPaymentProgressBinding
import app.onem.kioskandroid.utils.format
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

const val PAYMENT_REQUEST = "PAYMENT_REQUEST"
const val PAYMENT_REQUEST_DATA = "PAYMENT_REQUEST_DATA"

class PaymentFragment : BaseFragment(R.layout.fr_payment_progress) {

    companion object {
        private const val ALPHA_VISIBLE = 1f
        private const val ALPHA_HIDDEN = 0f
        private const val ANIMATION_DURATION_MS = 200L
    }

    private val args: PaymentFragmentArgs by navArgs()
    private val viewModel by viewModel<PaymentViewModel> {
        parametersOf(args.price, args.name, args.phone)
    }

    private val binding: FrPaymentProgressBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeButton.setOnClickListener { viewModel.onCancelClicked() }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.viewStateFlow.collect(::setViewState)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.actionFlow.collect(::runAction)
        }
    }

    private fun setViewState(state: PaymentViewState) {
        with(binding) {
            amountTextView.text = state.price?.format() ?: ""

            val outputStrings = state.texts.map {
                if (it is Int) getString(it) else it.toString()
            }
            val outputFields = listOf(primaryTextView, secondaryTextView)
            outputFields.forEachIndexed { index, textView ->
                textView.text = outputStrings.getOrNull(index)
            }

            val showLoadResult = state.indicator == PaymentProgressIndicator.ShowCheckmark ||
                state.indicator == PaymentProgressIndicator.ShowError
            if (loadingImage.alpha == ALPHA_HIDDEN && showLoadResult) {
                loadingImage.setImageResource(
                    when (state.indicator) {
                        PaymentProgressIndicator.ShowCheckmark -> R.drawable.ic_success
                        else -> R.drawable.ic_fail
                    }
                )
                loadingImage
                    .animate()
                    .alpha(ALPHA_VISIBLE)
                    .duration = ANIMATION_DURATION_MS
                progressAnimation.animate()
                    .alpha(ALPHA_HIDDEN)
                    .duration = ANIMATION_DURATION_MS
                progressFilled.visibility = View.VISIBLE
            }
            if (loadingImage.alpha == ALPHA_VISIBLE && !showLoadResult) {
                loadingImage
                    .animate()
                    .alpha(ALPHA_HIDDEN)
                    .duration = ANIMATION_DURATION_MS
                progressAnimation.animate()
                    .alpha(ALPHA_VISIBLE)
                    .duration = ANIMATION_DURATION_MS
                progressFilled.visibility = View.INVISIBLE
            }
        }
    }

    private fun runAction(action: PaymentAction) {
        when (action) {
            is PaymentAction.Proceed -> {
            }
            is PaymentAction.SendPaymentResult -> {
                setFragmentResult(
                    PAYMENT_REQUEST,
                    bundleOf(PAYMENT_REQUEST_DATA to action.paymentData)
                )
                findNavController().popBackStack()
            }
        }
    }
}
