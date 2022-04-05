package app.onem.kioskandroid.feature.setup.reader.location

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.onem.kioskandroid.R
import app.onem.kioskandroid.base.decorator.LineItemDecorator
import app.onem.kioskandroid.databinding.DLocationBinding
import app.onem.kioskandroid.feature.setup.reader.create_location.CreateLocationDialog
import by.kirich1409.viewbindingdelegate.viewBinding
import com.stripe.stripeterminal.external.models.Location
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.viewModel

class LocationDialog(private val onLocationSelected: ((location: Location) -> Unit)) :
    DialogFragment(R.layout.d_location) {

    private val viewModel by viewModel<LocationViewModel>()
    private val binding: DLocationBinding by viewBinding()

    private val locationsAdapter by lazy(LazyThreadSafetyMode.NONE) {
        LocationsAdapter(
            viewModel::locationSelected
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        observeViewModel()
        initRecycler()

        viewModel.fetchLocations()
    }

    private fun initRecycler() {
        binding.rvLocations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = locationsAdapter
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
            tvCreate.setOnClickListener {
                CreateLocationDialog { viewModel.fetchLocations() }
                    .show(childFragmentManager, CreateLocationDialog::class.java.name)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.viewStateFlow.collect {
                if (it.isFetching) {
                    binding.tvFoundLocation.setText(R.string.fetchingLocation)
                } else {
                    binding.tvFoundLocation.text = resources.getQuantityString(
                        R.plurals.locationsFound,
                        it.locations.size,
                        it.locations.size
                    )
                    locationsAdapter.items = it.locations
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.actionFlow.collect {
                when (it) {
                    is LocationDialogAction.LocationSelected -> {
                        onLocationSelected.invoke(it.location)
                        dismiss()
                    }
                }
            }
        }
    }
}