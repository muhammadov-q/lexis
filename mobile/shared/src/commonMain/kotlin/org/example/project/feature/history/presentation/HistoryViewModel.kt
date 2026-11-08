package org.example.project.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.core.mvi.setState
import org.example.project.feature.auth.domain.model.AuthApiException
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.session.SessionMode
import org.example.project.feature.history.domain.usecase.GetRecognitionHistoryUseCase
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.history_error_general
import scriptlens.shared.generated.resources.history_error_not_found
import scriptlens.shared.generated.resources.history_error_unauthorized

class HistoryViewModel(
    private val getRecognitionHistory: GetRecognitionHistoryUseCase,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryContract())
    val state = _state.asStateFlow()
    private var observersStarted: Boolean = false
    private var historyRequest: Job? = null
    private var requestId = 0L

    fun startObservers() {
        if (observersStarted) return
        observersStarted = true
        observeAuthMode()
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            HistoryEvent.LoadRequested -> loadHistory()
            HistoryEvent.RetryRequested -> loadHistory()
            HistoryEvent.DismissErrorRequested -> _state.setState { it.copy(error = null) }
        }
    }

    private fun loadHistory() {
        if (historyRequest?.isActive == true) return

        val currentRequestId = ++requestId
        historyRequest = viewModelScope.launch {
            _state.setState { it.copy(isLoading = true, error = null) }
            try {
                val items = getRecognitionHistory()
                if (currentRequestId != requestId) return@launch
                _state.setState {
                    it.copy(
                        isLoading = false,
                        items = items,
                        error = null,
                        requiresSignIn = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                if (currentRequestId != requestId) return@launch
                _state.setState {
                    it.copy(
                        isLoading = false,
                        error = historyError(exception),
                        requiresSignIn = exception is AuthApiException && exception.status == 401,
                    )
                }
            }
        }
    }

    private fun cancelHistoryRequest() {
        requestId++
        historyRequest?.cancel()
        historyRequest = null
    }

    private fun observeAuthMode() {
        viewModelScope.launch {
            authSessionStore.mode.collectLatest { mode ->
                if (mode is SessionMode.Guest) {
                    cancelHistoryRequest()
                    _state.setState {
                        it.copy(
                            items = emptyList(),
                            requiresSignIn = false,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
            }
        }
    }

}

internal fun historyError(exception: Exception): UiText {
    if (exception is AuthApiException) {
        return when (exception.status) {
            401 -> UiText.Resource(Res.string.history_error_unauthorized)
            404 -> UiText.Resource(Res.string.history_error_not_found)
            else -> UiText.Resource(Res.string.history_error_general)
        }
    }
    return UiText.Resource(Res.string.history_error_general)
}
