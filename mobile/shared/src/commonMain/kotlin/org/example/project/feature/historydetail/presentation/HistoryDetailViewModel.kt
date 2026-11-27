package org.example.project.feature.historydetail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.core.mvi.setState
import org.example.project.core.network.ApiException
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.presentation.resetOnSignOut
import org.example.project.feature.history.domain.usecase.GetRecognitionDetailUseCase
import org.example.project.feature.history.presentation.historyError

class HistoryDetailViewModel(
    private val getRecognitionDetail: GetRecognitionDetailUseCase,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryDetailContract())
    val state = _state.asStateFlow()
    private var request: Job? = null
    private var requestId = 0L

    init {
        resetOnSignOut(authSessionStore) {
            cancelRequest()
            _state.value = HistoryDetailContract()
        }
    }

    fun onEvent(event: HistoryDetailEvent) {
        when (event) {
            is HistoryDetailEvent.LoadRequested -> load(event.id, event.initialWordIndex)
            is HistoryDetailEvent.WordSelected -> selectWord(event.index)
        }
    }

    private fun load(id: Long, initialWordIndex: Int?) {
        if (_state.value.selectedDetail?.id == id) return
        request?.cancel()
        val currentRequestId = ++requestId
        _state.setState {
            it.copy(
                isLoading = true,
                error = null,
                selectedDetail = null,
                selectedPredictionIndex = null,
                requiresSignIn = false,
            )
        }
        request = viewModelScope.launch {
            try {
                val detail = getRecognitionDetail(id)
                if (currentRequestId != requestId) return@launch
                _state.setState {
                    it.copy(
                        isLoading = false,
                        selectedDetail = detail,
                        selectedPredictionIndex = initialWordIndex
                            ?.takeIf { index -> index in detail.prediction.predictions.indices },
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
                        requiresSignIn = exception is ApiException && exception.status == 401,
                    )
                }
            }
        }
    }

    private fun cancelRequest() {
        requestId++
        request?.cancel()
        request = null
    }

    private fun selectWord(index: Int?) {
        _state.setState { current ->
            val maxIndex = current.selectedDetail?.prediction?.predictions?.lastIndex ?: -1
            if (index == null || index in 0..maxIndex) current.copy(selectedPredictionIndex = index) else current
        }
    }
}
