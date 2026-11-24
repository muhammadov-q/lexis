package org.example.project.feature.vocabulary.presentation.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_error_add
import lexis.shared.generated.resources.vocabulary_error_save_note
import org.example.project.core.mvi.setState
import org.example.project.feature.auth.domain.model.AuthApiException
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.session.SessionMode
import org.example.project.feature.vocabulary.domain.repository.VocabularyRepository
import org.example.project.feature.vocabulary.presentation.vocabularyError

class WordNoteViewModel(
    private val repository: VocabularyRepository,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow(WordNoteContract())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.words.collect { words -> _state.setState { it.copy(words = words) } }
        }
        viewModelScope.launch {
            authSessionStore.mode.collect { mode ->
                if (mode is SessionMode.Guest) {
                    repository.clear()
                    _state.value = WordNoteContract()
                }
            }
        }
    }

    fun onEvent(event: WordNoteEvent) {
        when (event) {
            WordNoteEvent.LoadRequested -> load()
            is WordNoteEvent.SaveRequested -> save(event)
            WordNoteEvent.WordChanged -> _state.setState { it.copy(savedWord = null, error = null) }
        }
    }

    private fun load() {
        if (_state.value.words != null) return
        viewModelScope.launch {
            try {
                repository.refresh()
                _state.setState { it.copy(isLoadFailed = false) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _state.setState { it.copy(isLoadFailed = true) }
            }
        }
    }

    private fun save(event: WordNoteEvent.SaveRequested) {
        if (_state.value.isSaving) return
        val note = event.note.trim()
        _state.setState { it.copy(isSaving = true, savedWord = null, error = null) }

        viewModelScope.launch {
            val existing = _state.value.words?.find { it.word == event.headword }
            try {
                if (existing != null) {
                    repository.updateNote(existing.id, note)
                } else {
                    addOrUpdate(event, note)
                }
                _state.setState { it.copy(isSaving = false, savedWord = event.headword) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                val fallback = if (existing == null) Res.string.vocabulary_error_add else Res.string.vocabulary_error_save_note
                _state.setState { it.copy(isSaving = false, error = vocabularyError(exception, fallback)) }
            }
        }
    }

    private suspend fun addOrUpdate(event: WordNoteEvent.SaveRequested, note: String) {
        try {
            repository.add(event.recognitionId, event.wordIndex, note)
        } catch (exception: AuthApiException) {
            if (exception.status != 409) throw exception
            val saved = repository.refresh().find { it.word == event.headword } ?: throw exception
            repository.updateNote(saved.id, note)
        }
    }
}
