package app.onem.kioskandroid.feature.setup.reader.create_location

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import app.onem.kioskandroid.R
import app.onem.kioskandroid.databinding.DCreateLocationBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.stripe.stripeterminal.external.models.Location
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.viewModel

class CreateLocationDialog(private val onLocationCreated: (() -> Unit)) :
    DialogFragment(R.layout.d_create_location) {

    private val viewModel by viewModel<CreateLocationViewModel>()
    private val binding: DCreateLocationBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        observeViewModel()
    }

    private fun setListeners() {
        with(binding) {
            tvCancel.setOnClickListener { dismiss() }
            tvCreate.setOnClickListener {
                viewModel.createLocation(
                    displayName = etDisplayName.text.toString(),
                    lineFirst = etLineFirst.text.toString(),
                    lineSecond = etLineSecond.text.toString(),
                    city = etCity.text.toString(),
                    state = etState.text.toString(),
                    country = tvCountry.text.toString(),
                    postalCode = etPostalCode.text.toString()
                )
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.viewStateFlow.collect {
                with(binding) {
                    progressBar.isVisible = it.isProgressVisible
                    tvEmptyFieldError.isVisible = it.error != null
                    if (it.error != null) {
                        tvEmptyFieldError.text = resources.getString(
                            if (it.error == LocationEmptyFieldType.DisplayName) {
                                R.string.pleaseEnterADisplayName
                            } else {
                                R.string.pleaseEnterAAddressNLineFirst
                            }
                        )
                        scrollView.smoothScrollTo(0, 0)
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.actionFlow.collect {
                when (it) {
                    CreateLocationDialogAction.CloseDialogWithSuccess -> {
                        dismiss()
                        onLocationCreated.invoke()
                    }
                }
            }
        }
    }
}
