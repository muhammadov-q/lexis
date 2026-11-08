package org.example.project.feature.historydetail.presentation

import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.feature.history.domain.model.RecognitionDetail

data class HistoryDetailContract(
    val isLoading: Boolean = false,
    val selectedDetail: RecognitionDetail? = null,
    val selectedPredictionIndex: Int? = null,
    val requiresSignIn: Boolean = false,
    val error: UiText? = null,
)
