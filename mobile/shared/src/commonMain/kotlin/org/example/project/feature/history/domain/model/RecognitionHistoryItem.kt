package org.example.project.feature.history.domain.model

import org.example.project.feature.camera.domain.model.Prediction

data class RecognitionHistoryItem(
    val id: Long,
    val createdAt: String,
    val filename: String,
    val modelVersion: String,
    val previewText: String,
)

data class RecognitionDetail(
    val id: Long,
    val createdAt: String,
    val filename: String,
    val modelVersion: String,
    val prediction: Prediction,
    val imageBytes: ByteArray,
)

