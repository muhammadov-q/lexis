package org.example.project.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.core.mvi.setState
import org.example.project.core.network.ApiException
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.presentation.resetOnSignOut
import org.example.project.feature.history.domain.usecase.GetRecognitionHistoryUseCase
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.history_error_general
import lexis.shared.generated.resources.history_error_not_found
import lexis.shared.generated.resources.history_error_unauthorized

class HistoryViewModel(
    private val getRecognitionHistory: GetRecognitionHistoryUseCase,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryContract())
    val state = _state.asStateFlow()
    private var historyRequest: Job? = null
    private var requestId = 0L

    init {
        resetOnSignOut(authSessionStore) {
            cancelHistoryRequest()
            _state.value = HistoryContract()
        }
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
                        requiresSignIn = exception is ApiException && exception.isUnauthorized,
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
}

internal fun historyError(exception: Exception): UiText {
    if (exception is ApiException) {
        return when (exception.status) {
            ApiException.UNAUTHORIZED -> UiText.Resource(Res.string.history_error_unauthorized)
            ApiException.NOT_FOUND -> UiText.Resource(Res.string.history_error_not_found)
            else -> UiText.Resource(Res.string.history_error_general)
        }
    }
    return UiText.Resource(Res.string.history_error_general)
}
