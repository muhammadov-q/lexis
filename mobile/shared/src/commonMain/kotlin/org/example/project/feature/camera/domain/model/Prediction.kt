package org.example.project.feature.camera.domain.model

data class Prediction(
    val predictions: List<WordPrediction>,
    val modelVersion: String,
)

data class WordPrediction(
    val text: String,
    val probabilities: Map<String, Double>,
    val box: PredictionBox,
    val line: Int,
    val gloss: PredictionGloss,
)

data class PredictionBox(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
)

data class PredictionGloss(
    val lemma: String,
    val translation: String,
    val definition: String,
)

