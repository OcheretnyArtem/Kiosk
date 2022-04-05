package app.onem.kioskandroid.feature.informationcollection

import android.app.Dialog
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.InputFilter
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.onem.kioskandroid.R
import app.onem.kioskandroid.base.BaseFragment
import app.onem.kioskandroid.base.OrderCancelable
import app.onem.kioskandroid.databinding.DConfirmPhoneBinding
import app.onem.kioskandroid.databinding.FrInformationCollectionBinding
import app.onem.kioskandroid.feature.payment.PAYMENT_REQUEST
import app.onem.kioskandroid.feature.payment.PAYMENT_REQUEST_DATA
import app.onem.kioskandroid.feature.payment.PaymentData
import app.onem.kioskandroid.utils.hideKeyboard
import app.onem.kioskandroid.utils.showKeyboard
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale


const val INFORMATION_COLLECTION_REQUEST = "INFORMATION_COLLECTION_REQUEST"
const val INFORMATION_COLLECTION_REQUEST_DATA = "INFORMATION_COLLECTION_REQUEST_DATA"

class InformationCollectionFragment : BaseFragment(R.layout.fr_information_collection), OrderCancelable {

    private val args: InformationCollectionFragmentArgs by navArgs()
    private val viewModel by viewModel<InformationCollectionViewModel> {
        parametersOf(args.price)
    }
    private val binding by viewBinding<FrInformationCollectionBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(PAYMENT_REQUEST) { _, bundle ->
            val paymentResult = bundle.getParcelable<PaymentData>(PAYMENT_REQUEST_DATA)
            paymentResult?.let {
                sendPaymentResults(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            with(phoneEditText) {
                addTextChangedListener(PhoneNumberFormattingTextWatcher(Locale.US.country))
                setOnLongClickListener { true }
                filters = arrayOf(
                    InputFilter.LengthFilter(16),
                    InputFilter { source, start, end, dest, dstart, dend ->
                        if ((end - start ==  1) && !source[start].isDigit()) ""
                        else null
                    }
                )
            }

            with(nameEditText) {
                addTextChangedListener(TextChangeWatcher {
                    payButton.isEnabled = it.isNotEmpty()
                })

                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onPayClicked()
                    }
                    true
                }
            }

            payButton.setOnClickListener { onPayClicked() }

            closeButton.setOnClickListener { cancelOrderProcess() }
        }

        observeViewModel()
    }

    override fun cancelOrderProcess() {
        sendPaymentResults(PaymentData(isPaymentCompleted = false))
    }

    private fun onPayClicked() {
        with(binding) {
            val nameNotEmpty = nameEditText.text.isNotEmpty()
            val phoneValid = isPhoneNumberValid(phoneEditText.text.toString())

            nameErrorText.isInvisible = nameNotEmpty
            phoneErrorText.isInvisible = phoneValid
            if (nameNotEmpty && phoneValid) {
                viewModel.onFieldsFilled(
                    nameEditText.text.toString(),
                    "+${phoneEditText.text}"
                )
            }
        }
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return phoneNumber.count { it.isDigit() } >= 11
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.viewStateFlow.collect(::setViewState)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.actionFlow.collect(::runAction)
        }
    }

    private fun setViewState(state: InformationCollectionViewState) {
        with(binding) {
            progressIndicator.isVisible = state.isRequestProcess
            nameEditText.isEnabled = !state.isRequestProcess
            phoneEditText.isEnabled = !state.isRequestProcess
            payButton.isEnabled = !state.isRequestProcess
        }
    }

    private fun runAction(action: InformationCollectionAction) {
        when (action) {
            is InformationCollectionAction.StartPayment -> {
                navigate(
                    InformationCollectionFragmentDirections.actionInformationCollectionFragmentToPaymentFragment(
                        action.price,
                        action.name,
                        action.phone
                    )
                )
            }
            is InformationCollectionAction.ConfirmPhone -> {
                binding.root.findFocus().hideKeyboard()
                openPhoneConfirmationDialog(action.phone)
            }
            is InformationCollectionAction.SendPaymentResult -> sendPaymentResults(action.paymentData)
        }
    }

    private fun openPhoneConfirmationDialog(phone: String) {
        val alertBinding = DConfirmPhoneBinding.inflate(layoutInflater)
        alertBinding.tvPhone.text = phone
        val confirmationDialog = AlertDialog.Builder(requireContext())
            .setPositiveButton(R.string.continueButton) { _, _ ->
                viewModel.onPhoneConfirmed()
            }
            .setNegativeButton(R.string.editButton) { _, _ ->
                binding.phoneEditText.apply {
                    requestFocus()
                    showKeyboard()
                }
            }
            .setView(alertBinding.root)
            .create()

        confirmationDialog.setOnShowListener {
            listOf(Dialog.BUTTON_POSITIVE, Dialog.BUTTON_NEGATIVE).map {
                confirmationDialog.getButton(it)
            }.forEach {
                it.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.informationCollectionButtonSize))
            }
        }
        confirmationDialog
            .show()
    }

    private fun sendPaymentResults(paymentData: PaymentData) {
        setFragmentResult(
            INFORMATION_COLLECTION_REQUEST,
            bundleOf(INFORMATION_COLLECTION_REQUEST_DATA to paymentData)
        )
        findNavController().popBackStack()
    }
}
