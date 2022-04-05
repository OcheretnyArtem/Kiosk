package app.onem.kioskandroid.feature.setup.alert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import app.onem.kioskandroid.R
import app.onem.kioskandroid.databinding.DSetupAlertBinding
import app.onem.kioskandroid.utils.putEnum
import app.onem.kioskandroid.utils.requireEnum
import app.onem.kioskandroid.utils.setArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class SetupAlert : AppCompatDialogFragment() {

    companion object {

        private const val ALERT_TYPE_KEY = "ALERT_TYPE_KEY"

        fun newInstance(alertType: SetupAlertType): SetupAlert =
            SetupAlert().setArgs {
                putEnum(ALERT_TYPE_KEY, alertType)
            }

        private val _eventChannel: Channel<SetupAlertAction> =
            Channel(Channel.BUFFERED)

        val actionFlow: Flow<SetupAlertAction> =
            _eventChannel.receiveAsFlow()
    }

    private val binding: DSetupAlertBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.d_setup_alert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        val args = requireArguments()
        val alertType: SetupAlertType = args.requireEnum(ALERT_TYPE_KEY)

        val messageId = alertType.messageId
        val buttonTextId = alertType.buttonTextId

        binding.title.setText(messageId)

        binding.actionButton.text = buttonTextId?.let(this::getString)
        binding.actionButton.setOnClickListener {
            _eventChannel.trySend(
                SetupAlertAction.ButtonClicked(alertType)
            )
        }
        binding.actionButton.isVisible = (buttonTextId != null)
    }
}
