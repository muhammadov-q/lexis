package org.example.project.feature.camera.presentation.models

data class PredictionModel (
    val predictions: List<WordPredictionModel>,
    val modelVersion: String,
)

data class WordPredictionModel(
    val text: String,
    val probabilities: Map<String, Double>,
    val box: PredictionBoxModel,
    val line: Int,
    val gloss: PredictionGlossModel,
)

data class PredictionBoxModel(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
)

data class PredictionGlossModel(
    val lemma: String,
    val translation: String,
    val definition: String,
)

