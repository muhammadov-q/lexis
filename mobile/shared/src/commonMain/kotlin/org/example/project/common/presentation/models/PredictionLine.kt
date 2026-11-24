package org.example.project.common.presentation.models

data class PredictionLine(
    val number: Int,
    val words: List<IndexedValue<WordPredictionModel>>,
    val translation: String?,
)
