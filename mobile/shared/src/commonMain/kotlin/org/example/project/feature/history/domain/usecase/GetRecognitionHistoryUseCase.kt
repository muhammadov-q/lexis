package org.example.project.feature.history.domain.usecase

import org.example.project.feature.history.domain.model.RecognitionHistoryItem
import org.example.project.feature.history.domain.repository.HistoryRepository

class GetRecognitionHistoryUseCase(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(): List<RecognitionHistoryItem> = repository.getHistory()
}

