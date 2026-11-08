package org.example.project.feature.history.domain.usecase

import org.example.project.feature.history.domain.model.RecognitionDetail
import org.example.project.feature.history.domain.repository.HistoryRepository

class GetRecognitionDetailUseCase(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(id: Long): RecognitionDetail = repository.getRecognitionDetail(id)
}

