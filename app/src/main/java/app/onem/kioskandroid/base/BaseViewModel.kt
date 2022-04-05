package app.onem.kioskandroid.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel<VS : ViewState?, A : Action> : ViewModel(), CoroutineScope {

    protected abstract fun initialViewState(): VS

    private val _eventChannel = Channel<A>(Channel.BUFFERED)
    internal val actionFlow: Flow<A> get() = _eventChannel.receiveAsFlow()

    private val _viewStateFlow: MutableStateFlow<VS> = MutableStateFlow(initialViewState())
    val viewStateFlow: StateFlow<VS> = _viewStateFlow

    internal fun sendAction(action: A) {
        launch {
            _eventChannel.send(action)
        }
    }

    protected fun reduceViewState(reducer: (VS) -> VS) {
        launch {
            _viewStateFlow.emit(reducer(viewStateFlow.value))
        }
    }

    private val defaultCoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    override val coroutineContext: CoroutineContext
        get() = viewModelScope.coroutineContext + defaultCoroutineExceptionHandler
}
