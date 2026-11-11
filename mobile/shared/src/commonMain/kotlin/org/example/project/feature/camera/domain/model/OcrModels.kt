package org.example.project.feature.camera.domain.model

data class OcrModel(val id: String, val name: String, val description: String)

data class OcrModels(val models: List<OcrModel>, val defaultModel: String)
