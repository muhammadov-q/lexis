package org.example.project.feature.vocabulary.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_error_load
import org.example.project.core.mvi.setState
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.presentation.resetOnSignOut
import org.example.project.feature.vocabulary.domain.repository.VocabularyRepository
import org.example.project.feature.vocabulary.presentation.isUnauthorized
import org.example.project.feature.vocabulary.presentation.vocabularyError

class VocabularyViewModel(
    private val repository: VocabularyRepository,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow(VocabularyContract())
    val state = _state.asStateFlow()
    private var request: Job? = null

    init {
        viewModelScope.launch {
            repository.words.collect { words -> _state.setState { it.copy(words = words) } }
        }
        resetOnSignOut(authSessionStore) {
            request?.cancel()
            _state.value = VocabularyContract()
        }
    }

    fun onEvent(event: VocabularyEvent) {
        when (event) {
            VocabularyEvent.LoadRequested, VocabularyEvent.RetryRequested -> load()
        }
    }

    private fun load() {
        if (request?.isActive == true) return
        _state.setState { it.copy(isLoading = true, error = null, requiresSignIn = false) }
        request = viewModelScope.launch {
            try {
                repository.refresh()
                _state.setState { it.copy(isLoading = false) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _state.setState {
                    it.copy(
                        isLoading = false,
                        error = vocabularyError(exception, Res.string.vocabulary_error_load),
                        requiresSignIn = exception.isUnauthorized(),
                    )
                }
            }
        }
    }
}
