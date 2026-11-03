package org.example.project.feature.camera.data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class PredictionItemDto(
    val text: String,
    val probabilities: Map<String, Double>,
    val box: BoxDto,
    val line: Int,
    val gloss: GlossDto?,
)


