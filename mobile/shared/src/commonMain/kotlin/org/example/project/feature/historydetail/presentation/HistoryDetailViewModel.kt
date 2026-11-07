package org.example.project.feature.historydetail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.core.mvi.setState
import org.example.project.feature.auth.domain.model.AuthApiException
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.session.SessionMode
import org.example.project.feature.history.domain.usecase.GetRecognitionDetailUseCase
import org.example.project.feature.history.presentation.historyError

class HistoryDetailViewModel(
    private val getRecognitionDetail: GetRecognitionDetailUseCase,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryDetailContract())
    val state = _state.asStateFlow()
    private var request: Job? = null

    init {
        viewModelScope.launch {
            authSessionStore.mode.collect { mode ->
                if (mode is SessionMode.Guest) {
                    request?.cancel()
                    _state.value = HistoryDetailContract()
                }
            }
        }
    }

    fun load(id: Long) {
        if (request?.isActive == true) return
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
                _state.setState {
                    it.copy(
                        isLoading = false,
                        selectedDetail = detail,
                        selectedPredictionIndex = detail.prediction.predictions.indices.firstOrNull(),
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                if (exception is AuthApiException && exception.status == 401) {
                    authSessionStore.setGuest()
                }
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

    fun selectPrediction(index: Int) {
        _state.setState { current ->
            val maxIndex = current.selectedDetail?.prediction?.predictions?.lastIndex ?: -1
            if (index in 0..maxIndex) current.copy(selectedPredictionIndex = index) else current
        }
    }
}
