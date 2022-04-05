package app.onem.kioskandroid.feature.setup.reader

import app.onem.kioskandroid.R
import app.onem.kioskandroid.databinding.ItemReaderBinding
import app.onem.kioskandroid.itemCallback
import app.onem.kioskandroid.utils.getCroppedSerial
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.Reader

private fun readerDelegate(
    onItemClicked: (reader: ReaderItem) -> Unit
) =
    adapterDelegateViewBinding<ReaderItem, ReaderItem, ItemReaderBinding>(
        { layoutInflater, parent ->
            ItemReaderBinding.inflate(
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
                tvReaderTitle.text = item.reader.getCroppedSerial()
                val sameLocation = item.reader.location?.id?.equals(item.location?.id) ?: false
                tvReaderLocation.text = if (sameLocation) {
                    item.location?.displayName
                } else {
                    getString(
                        R.string.location_will_be_changed,
                        item.reader.location?.displayName ?: "",
                        item.location?.displayName ?: ""
                    )
                }
            }
        }
    }

private val itemDiffCallback = itemCallback<ReaderItem>(
    areItemsTheSame = { oldItem, newItem ->
        oldItem.reader.id == newItem.reader.id
    },
    areContentsTheSame = { oldItem, newItem ->
        oldItem == newItem
    },
    getChangePayload = { _, newItem ->
        newItem
    }
)

class ReadersAdapter(
    onItemClicked: (reader: ReaderItem) -> Unit
) : AsyncListDifferDelegationAdapter<ReaderItem>(
    itemDiffCallback
) {
    init {
        delegatesManager.addDelegate(readerDelegate(onItemClicked))
    }
}

data class ReaderItem(val reader: Reader, val location: Location?)