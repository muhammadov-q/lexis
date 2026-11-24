package org.example.project.feature.vocabulary.presentation.word

import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.feature.vocabulary.domain.model.VocabularyWord

data class VocabularyWordContract(
    val isLoading: Boolean = false,
    val word: VocabularyWord? = null,
    val requiresSignIn: Boolean = false,
    val error: UiText? = null,
    val isSavingNote: Boolean = false,
    val isNoteSaved: Boolean = false,
    val noteError: UiText? = null,
    val isRemoveDialogShown: Boolean = false,
    val isRemoving: Boolean = false,
    val removeError: UiText? = null,
)

sealed interface VocabularyWordEvent {
    data class LoadRequested(val id: Long) : VocabularyWordEvent
    data class SaveNoteRequested(val note: String) : VocabularyWordEvent
    data object NoteEdited : VocabularyWordEvent
    data object RemoveRequested : VocabularyWordEvent
    data object RemoveDismissed : VocabularyWordEvent
    data object RemoveConfirmed : VocabularyWordEvent
}

sealed interface VocabularyWordEffect {
    data class Removed(val word: String) : VocabularyWordEffect
}
