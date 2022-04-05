package app.onem.kioskandroid.feature.setup.shop

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.onem.domain.utils.Shop
import app.onem.kioskandroid.R
import app.onem.kioskandroid.databinding.DShopChooserBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.viewModel
import reactivecircus.flowbinding.appcompat.queryTextChanges

class ChooseShopDialog(private val onShopSelected: ((shop: Shop) -> Unit)? = null) :
    DialogFragment(R.layout.d_shop_chooser) {

    private val viewModel by viewModel<ChooseShopViewModel>()
    private val binding: DShopChooserBinding by viewBinding()

    private val shopsAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ShopsAdapter(
            viewModel::onShopClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViews()
        observeViewModel()
        initRecycler()
    }

    private fun observeViews() {
        lifecycleScope.launchWhenStarted {
            binding.searchView
                .queryTextChanges()
                .collect(viewModel::onQueryTextChanged)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.viewStateFlow.collect(::setViewState)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.actionFlow.collect(::runAction)
        }
    }

    private fun setViewState(state: ShopDialogViewState) {
        val isLoading = state.isLoading
        binding.progress.isVisible = isLoading

        val shopsList = state.shopsList
        binding.rvShops.isInvisible = shopsList.isEmpty()
        shopsAdapter.items = shopsList

        val error = state.error
        binding.tvErrorMessage.isVisible = (error != null)
        binding.tvErrorMessage.text = error?.toLocalizedString(requireContext())
    }

    private fun runAction(action: ShopDialogAction) {
        when (action) {
            is ShopDialogAction.HideDialog -> {
                onShopSelected?.invoke(action.selectedShop)
                dismiss()
            }
        }
    }

    private fun initRecycler() {
        binding.rvShops.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shopsAdapter
        }
    }

}