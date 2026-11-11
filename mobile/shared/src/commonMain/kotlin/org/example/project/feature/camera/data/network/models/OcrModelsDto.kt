package org.example.project.feature.camera.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OcrModelDto(val id: String, val name: String, val description: String)

@Serializable
data class OcrModelsDto(
    val models: List<OcrModelDto>,
    @SerialName("default_model") val defaultModel: String,
)
