package org.example.project.feature.vocabulary.presentation.note

import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.feature.vocabulary.domain.model.VocabularyWord

data class WordNoteContract(
    val words: List<VocabularyWord>? = null,
    val isLoadFailed: Boolean = false,
    val isSaving: Boolean = false,
    val savedWord: String? = null,
    val error: UiText? = null,
)

sealed interface WordNoteEvent {
    data object LoadRequested : WordNoteEvent
    data class SaveRequested(
        val recognitionId: Long,
        val wordIndex: Int,
        val headword: String,
        val note: String,
    ) : WordNoteEvent
    data object WordChanged : WordNoteEvent
}
