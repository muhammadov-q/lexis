package org.example.project.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.core.mvi.setState
import org.example.project.feature.auth.domain.model.AuthApiException
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.session.SessionMode
import org.example.project.feature.history.domain.session.HistoryRefreshStore
import org.example.project.feature.history.domain.usecase.GetRecognitionHistoryUseCase

private const val HISTORY_ERROR_UNAUTHORIZED = "Sign in to load your recognition history."
private const val HISTORY_ERROR_NOT_FOUND = "Saved recognition was not found."
private const val HISTORY_ERROR_GENERAL = "Could not load recognition history. Please try again."

class HistoryViewModel(
    private val getRecognitionHistory: GetRecognitionHistoryUseCase,
    private val authSessionStore: AuthSessionStore,
    private val historyRefreshStore: HistoryRefreshStore,
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryContract())
    val state = _state.asStateFlow()
    private var observersStarted: Boolean = false

    fun startObservers() {
        if (observersStarted) return
        observersStarted = true
        observeAuthMode()
        observeHistoryRefresh()
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            HistoryEvent.LoadRequested -> loadHistory()
            HistoryEvent.RetryRequested -> loadHistory()
            HistoryEvent.DismissErrorRequested -> _state.setState { it.copy(error = null) }
        }
    }

    private fun loadHistory() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.setState { it.copy(isLoading = true, error = null) }
            try {
                val items = getRecognitionHistory()
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
                if (isUnauthorized(exception)) authSessionStore.setGuest()
                _state.setState {
                    it.copy(
                        isLoading = false,
                        error = historyError(exception),
                        requiresSignIn = isUnauthorized(exception),
                    )
                }
            }
        }
    }

    private fun isUnauthorized(exception: Exception): Boolean =
        exception is AuthApiException && exception.status == 401

    private fun observeAuthMode() {
        viewModelScope.launch {
            authSessionStore.mode.collectLatest { mode ->
                if (mode is SessionMode.Guest) {
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

    private fun observeHistoryRefresh() {
        viewModelScope.launch {
            historyRefreshStore.refreshVersion
                .drop(1)
                .collectLatest {
                    if (authSessionStore.isAuthenticated()) {
                        loadHistory()
                    }
                }

        }
    }
}

internal fun historyError(exception: Exception): UiText {
    if (exception is AuthApiException) {
        return when (exception.status) {
            401 -> UiText.Plain(HISTORY_ERROR_UNAUTHORIZED)
            404 -> UiText.Plain(HISTORY_ERROR_NOT_FOUND)
            else -> UiText.Plain(HISTORY_ERROR_GENERAL)
        }
    }
    return UiText.Plain(HISTORY_ERROR_GENERAL)
}
