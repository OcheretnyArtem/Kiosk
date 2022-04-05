package app.onem.kioskandroid.feature.setup.reader.location

import app.onem.kioskandroid.databinding.ItemLocationBinding
import app.onem.kioskandroid.itemCallback
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader

private fun locationDelegate(
    onItemClicked: (location: Location) -> Unit
) =
    adapterDelegateViewBinding<Location, Location, ItemLocationBinding>(
        { layoutInflater, parent ->
            ItemLocationBinding.inflate(
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
                tvTitle.text = item.displayName
                tvId.text = item.id
            }
        }
    }

private val itemDiffCallback = itemCallback<Location>(
    areItemsTheSame = { oldItem, newItem ->
        oldItem.id == newItem.id
    },
    areContentsTheSame = { oldItem, newItem ->
        oldItem == newItem
    },
    getChangePayload = { _, newItem ->
        newItem
    }
)

class LocationsAdapter(
    onItemClicked: (location: Location) -> Unit
) : AsyncListDifferDelegationAdapter<Location>(
    itemDiffCallback
) {
    init {
        delegatesManager.addDelegate(locationDelegate(onItemClicked))
    }
}