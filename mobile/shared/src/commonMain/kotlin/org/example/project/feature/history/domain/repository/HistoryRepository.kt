package org.example.project.feature.history.domain.repository

import org.example.project.feature.history.domain.model.RecognitionDetail
import org.example.project.feature.history.domain.model.RecognitionHistoryItem

interface HistoryRepository {
    suspend fun getHistory(): List<RecognitionHistoryItem>
    suspend fun getRecognitionDetail(id: Long): RecognitionDetail
}

