package org.example.project.feature.vocabulary.presentation.word

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_error_load_word
import lexis.shared.generated.resources.vocabulary_error_remove
import lexis.shared.generated.resources.vocabulary_error_save_note
import org.example.project.core.mvi.setEffect
import org.example.project.core.mvi.setState
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.presentation.resetOnSignOut
import org.example.project.feature.vocabulary.domain.repository.VocabularyRepository
import org.example.project.feature.vocabulary.presentation.isUnauthorized
import org.example.project.feature.vocabulary.presentation.vocabularyError

class VocabularyWordViewModel(
    private val repository: VocabularyRepository,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow(VocabularyWordContract())
    val state = _state.asStateFlow()

    private val effectChannel = Channel<VocabularyWordEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var request: Job? = null

    init {
        resetOnSignOut(authSessionStore) {
            request?.cancel()
            _state.value = VocabularyWordContract()
        }
    }

    fun onEvent(event: VocabularyWordEvent) {
        when (event) {
            is VocabularyWordEvent.LoadRequested -> load(event.id)
            is VocabularyWordEvent.SaveNoteRequested -> saveNote(event.note)
            VocabularyWordEvent.NoteEdited -> _state.setState { it.copy(isNoteSaved = false, noteError = null) }
            VocabularyWordEvent.RemoveRequested -> _state.setState { it.copy(isRemoveDialogShown = true, removeError = null) }
            VocabularyWordEvent.RemoveDismissed -> _state.setState { it.copy(isRemoveDialogShown = false, removeError = null) }
            VocabularyWordEvent.RemoveConfirmed -> remove()
        }
    }

    private fun load(id: Long) {
        request?.cancel()
        val cached = repository.words.value?.find { it.id == id }
        _state.setState {
            VocabularyWordContract(isLoading = cached == null, word = cached)
        }
        request = viewModelScope.launch {
            try {
                val word = repository.get(id)
                _state.setState { it.copy(isLoading = false, word = word) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _state.setState {
                    it.copy(
                        isLoading = false,
                        word = null,
                        error = vocabularyError(exception, Res.string.vocabulary_error_load_word),
                        requiresSignIn = exception.isUnauthorized(),
                    )
                }
            }
        }
    }

    private fun saveNote(note: String) {
        val word = _state.value.word ?: return
        if (_state.value.isSavingNote) return
        _state.setState { it.copy(isSavingNote = true, isNoteSaved = false, noteError = null) }
        viewModelScope.launch {
            try {
                val saved = repository.updateNote(word.id, note.trim())
                _state.setState { it.copy(word = saved, isSavingNote = false, isNoteSaved = true) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _state.setState {
                    it.copy(
                        isSavingNote = false,
                        noteError = vocabularyError(exception, Res.string.vocabulary_error_save_note),
                    )
                }
            }
        }
    }

    private fun remove() {
        val word = _state.value.word ?: return
        if (_state.value.isRemoving) return
        _state.setState { it.copy(isRemoving = true, removeError = null) }
        viewModelScope.launch {
            try {
                repository.remove(word.id)
                _state.setState { it.copy(isRemoving = false, isRemoveDialogShown = false) }
                effectChannel.setEffect(VocabularyWordEffect.Removed(word.word))
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _state.setState {
                    it.copy(
                        isRemoving = false,
                        removeError = vocabularyError(exception, Res.string.vocabulary_error_remove),
                    )
                }
            }
        }
    }
}
