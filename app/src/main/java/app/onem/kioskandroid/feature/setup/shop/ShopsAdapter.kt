package app.onem.kioskandroid.feature.setup.shop

import app.onem.domain.utils.Shop
import app.onem.kioskandroid.databinding.ItemShopBinding
import app.onem.kioskandroid.itemCallback
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

private fun shopDelegate(
    onItemClicked: (shop: Shop) -> Unit
) =
    adapterDelegateViewBinding<Shop, Shop, ItemShopBinding>(
        { layoutInflater, parent ->
            ItemShopBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        }

    ) {
        binding.root.setOnClickListener {
            onItemClicked(item)
        }

        bind {
            with(binding) {
                tvShop.text = item.value
            }
        }
    }

private val itemDiffCallback = itemCallback<Shop>(
    areItemsTheSame = { oldItem, newItem ->
        oldItem.id == newItem.id
    },
    areContentsTheSame = { oldItem, newItem ->
        oldItem == newItem
    }
)

class ShopsAdapter(
    onItemClicked: (shop: Shop) -> Unit
) : AsyncListDifferDelegationAdapter<Shop>(
    itemDiffCallback
) {
    init {
        delegatesManager.addDelegate(shopDelegate(onItemClicked))
    }
}
