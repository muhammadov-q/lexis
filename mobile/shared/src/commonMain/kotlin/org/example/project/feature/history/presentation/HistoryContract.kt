package org.example.project.feature.history.presentation

import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.feature.history.domain.model.RecognitionHistoryItem

data class HistoryContract(
    val isLoading: Boolean = false,
    val items: List<RecognitionHistoryItem> = emptyList(),
    val requiresSignIn: Boolean = false,
    val error: UiText? = null,
)

sealed interface HistoryEvent {
    data object LoadRequested : HistoryEvent
    data object RetryRequested : HistoryEvent
    data object DismissErrorRequested : HistoryEvent
}
