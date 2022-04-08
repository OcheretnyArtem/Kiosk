package app.onem.kioskandroid.feature.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.onem.kioskandroid.R
import app.onem.kioskandroid.base.BaseFragment
import app.onem.kioskandroid.base.OrderCancelable
import app.onem.kioskandroid.databinding.FrWebViewBinding
import app.onem.kioskandroid.feature.informationcollection.INFORMATION_COLLECTION_REQUEST
import app.onem.kioskandroid.feature.informationcollection.INFORMATION_COLLECTION_REQUEST_DATA
import app.onem.kioskandroid.feature.payment.PaymentData
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.viewModel


class WebViewFragment : BaseFragment(R.layout.fr_web_view), OrderCancelable {

    private val viewModel by viewModel<WebViewViewModel>()
    private val binding: FrWebViewBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(INFORMATION_COLLECTION_REQUEST) { _, bundle ->
            val paymentResult =
                bundle.getParcelable<PaymentData>(INFORMATION_COLLECTION_REQUEST_DATA)
            paymentResult?.let {
                viewModel.paymentResultReceived(paymentResult)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return if (viewModel.fragment == null) {
            super.onCreateView(inflater, container, savedInstanceState)
        } else {
            viewModel.fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.fragment == null) {
            initWebView()
        }
        observeViewModel()
        setListeners()
    }

    override fun onStop() {
        viewModel.fragment = view
        super.onStop()
    }

    override fun cancelOrderProcess() {
        // TODO Need to clear cart. Now it's not supported by Web app
        val json = "\"key\":\"UWU7yo2b\", \"cancelled\":true}"
        binding.webview.evaluateJavascript(
            "window.onPaymentMessage(JSON.stringify($json))"
        ) {}
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.viewStateFlow.collect(::setViewState)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.actionFlow.collect(::runAction)
        }
    }

    private fun setViewState(state: WebViewViewState) {
        with(binding) {

        }
    }

    private fun runAction(action: WebViewAction) {
        when (action) {
            WebViewAction.ShopReceived -> viewModel.runServer()
            is WebViewAction.LoadShop -> {
                action.shopId?.let {
                    openPage(it)
                }
            }
            is WebViewAction.ShowFailScreen -> {
                action.shopId?.let {
                    with(action.paymentData) {
                        val json =
                            "{\"phone\":\"$phone\",\"key\":\"UWU7yo2b\",\"payment_intent_id\":\"$paymentIntentId\",\"name\":\"$name\", \"cancelled\":true}"
                        binding.webview.evaluateJavascript(
                            "window.onPaymentMessage(JSON.stringify($json))"
                        ) {}
                    }
                }
            }
            is WebViewAction.ShowSuccessScreen -> {
                action.shopId?.let {
                    with(action.paymentData) {
                        val json =
                            "{\"phone\":\"$phone\",\"key\":\"UWU7yo2b\",\"payment_intent_id\":\"$paymentIntentId\",\"name\":\"$name\"}"
                        binding.webview.evaluateJavascript(
                            "window.onPaymentMessage(JSON.stringify($json))"
                        ) {}
                    }
                }
            }
            WebViewAction.ShowSettingsScreen -> {
                stopServer()
                findNavController().popBackStack()
            }
            is WebViewAction.ShowInformationCollectionScreen -> {
                navigate(
                    WebViewFragmentDirections.actionWebViewFragmentToInformationCollectionFragment(
                        action.price
                    )
                )
            }
        }
    }

    private fun setListeners() {
        with(binding) {
            homeButton.setOnClickListener { viewModel.onHomeButtonClicked() }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        with(binding.webview) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportZoom(false)
            addJavascriptInterface(WebViewInterface(this@WebViewFragment), "checkoutMessageHandler")
            webViewClient = WebViewClient()
        }
    }

    private fun openPage(url: String) {
        binding.webview.loadUrl("http://localhost:3000/index.html")
    }

    internal fun callPaymentClicked(price: Double) {
        viewModel.onPaymentClicked(price)
    }

    internal fun stopServer() {
        viewModel.stopServer()
    }
}

class WebViewInterface(private val fragment: WebViewFragment) {

    @JavascriptInterface
    fun postMessage(price: Double) {
        fragment.callPaymentClicked(price)
    }

}