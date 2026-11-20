package org.example.project.feature.camera.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PredictionItemDto(
    val text: String,
    val probabilities: Map<String, Double>,
    val box: BoxDto,
    val line: Int = 0,
    val gloss: GlossDto? = null,
    @SerialName("line_translation") val lineTranslation: String? = null,
)

