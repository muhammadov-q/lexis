package org.example.project.common.presentation.helpers

import org.example.project.common.presentation.models.WordPredictionModel

data class PredictionLine(
    val number: Int,
    val words: List<IndexedValue<WordPredictionModel>>,
    val translation: String?,
)

fun groupPredictionLines(words: List<WordPredictionModel>): List<PredictionLine> {
    val lines = mutableListOf<PredictionLine>()
    var index = 0
    while (index < words.size) {
        val number = words[index].line
        val lineWords = mutableListOf<IndexedValue<WordPredictionModel>>()
        var translation: String? = null
        while (index < words.size && words[index].line == number) {
            val word = words[index]
            lineWords.add(IndexedValue(index, word))
            if (translation == null) {
                translation = word.lineTranslation?.takeIf { it.isNotBlank() }
            }
            index++
        }
        lines.add(PredictionLine(number, lineWords, translation))
    }
    return lines
}

fun WordPredictionModel.isConfirmed(): Boolean = probabilities.containsKey(text)
