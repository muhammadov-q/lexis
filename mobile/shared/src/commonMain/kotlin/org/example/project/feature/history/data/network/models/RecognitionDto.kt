package org.example.project.feature.history.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.feature.camera.data.network.models.PredictionItemDto

@Serializable
data class RecognitionDto(
    val id: Long,
    @SerialName("created_at") val createdAt: String,
    val filename: String,
    @SerialName("model_version") val modelVersion: String,
    val predictions: List<PredictionItemDto>,
)

