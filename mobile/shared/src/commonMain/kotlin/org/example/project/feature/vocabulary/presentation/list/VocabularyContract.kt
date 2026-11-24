package org.example.project.feature.vocabulary.presentation.list

import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.feature.vocabulary.domain.model.VocabularyWord

data class VocabularyContract(
    val isLoading: Boolean = false,
    val words: List<VocabularyWord>? = null,
    val requiresSignIn: Boolean = false,
    val error: UiText? = null,
)

sealed interface VocabularyEvent {
    data object LoadRequested : VocabularyEvent
    data object RetryRequested : VocabularyEvent
}
