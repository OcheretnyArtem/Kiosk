package app.onem.kioskandroid.feature.setup.reader

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.onem.kioskandroid.R
import app.onem.kioskandroid.base.decorator.LineItemDecorator
import app.onem.kioskandroid.databinding.DTerminalBinding
import app.onem.kioskandroid.feature.setup.reader.location.LocationDialog
import app.onem.kioskandroid.utils.EmulatorTools
import by.kirich1409.viewbindingdelegate.viewBinding
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.viewModel

class TerminalDialog(
    private val onLocationSelected: ((Location) -> Unit)? = null,
    private val onTerminalSelected: ((Reader) -> Unit)? = null
) :
    DialogFragment(R.layout.d_terminal) {

    private val viewModel by viewModel<TerminalDialogViewModel>()
    private val binding: DTerminalBinding by viewBinding()

    private val readersAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ReadersAdapter(
            viewModel::readerSelected
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onLocationSelected = onLocationSelected
        viewModel.onReaderSelected = onTerminalSelected
        binding.tvLocationDescription.setText(
            if (EmulatorTools.isEmulator()) {
                R.string.tvMockLocationDescription
            } else {
                R.string.tvRealLocationDescription
            }
        )
        setListeners()
        observeViewModel()
        initRecycler()
    }

    override fun onStart() {
        super.onStart()
        viewModel.startDiscovering()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopDiscovering()
    }

    private fun initRecycler() {
        binding.rvTerminals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = readersAdapter
            addItemDecoration(
                LineItemDecorator(
                    context = requireContext(),
                    lineColor = R.color.tintTwo,
                    lineWidthId = R.dimen.divider_width,
                    startMarginId = R.dimen.zeroMargin,
                    endMarginId = R.dimen.zeroMargin
                )
            )
        }
    }

    private fun setListeners() {
        with(binding) {
            tvCancel.setOnClickListener { dismiss() }
            tvlocation.setOnClickListener { viewModel.onLocationClicked() }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.viewStateFlow.collect {
                with(binding.tvlocation) {
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (it.isEmulator) R.color.text_grey else R.color.black
                        )
                    )

                    if (it.isEmulator) {
                        setText(R.string.mockLocation)
                    } else {
                        if (it.location == null) {
                            setText(R.string.noLocationSelected)
                        } else {
                            text = it.location?.displayName
                        }
                    }
                }
                readersAdapter.items = it.readersList.map { item ->
                    ReaderItem(
                        reader = item,
                        location = it.location ?: item.location
                    )
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.actionFlow.collect {
                when (it) {
                    TerminalDialogAction.HideDialog -> dismiss()
                    TerminalDialogAction.ShowLocationDialog -> {
                        LocationDialog { location -> viewModel.onLocationSelected(location) }
                            .show(childFragmentManager, LocationDialog::class.java.name)
                    }
                    TerminalDialogAction.NoLocationForReader -> {
                        AlertDialog.Builder(requireContext())
                            .setMessage(R.string.noRegisteredLocation)
                            .setPositiveButton(R.string.ok) { _, _ -> }
                            .show()
                    }
                }
            }
        }
    }
}