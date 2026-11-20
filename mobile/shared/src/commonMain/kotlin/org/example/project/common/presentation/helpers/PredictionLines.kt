package org.example.project.common.presentation.helpers

import org.example.project.common.presentation.models.PredictionLine
import org.example.project.common.presentation.models.WordPredictionModel

fun groupPredictionLines(words: List<WordPredictionModel>): List<PredictionLine> {
    val lines = linkedMapOf<Int, PredictionLine>()
    words.forEachIndexed { index, word ->
        val line = lines[word.line] ?: PredictionLine(
            number = word.line,
            words = emptyList(),
            translation = word.lineTranslation?.takeIf { it.isNotBlank() },
        )
        lines[word.line] = line.copy(words = line.words + IndexedValue(index, word))
    }
    return lines.values.toList()
}

fun WordPredictionModel.isConfirmed(): Boolean = probabilities.containsKey(text)
