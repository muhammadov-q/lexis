package org.example.project.common.presentation.ui.preview

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import org.example.project.common.presentation.models.PredictionBoxModel
import org.example.project.common.presentation.models.PredictionGlossModel
import org.example.project.common.presentation.models.PredictionModel
import org.example.project.common.presentation.models.WordPredictionModel

private fun previewWord(
    text: String,
    translation: String,
    line: Int,
    position: Int,
    lineTranslation: String? = null,
    confirmed: Boolean = true,
) = WordPredictionModel(
    text = text,
    probabilities = if (confirmed) mapOf(text to 0.95) else emptyMap(),
    box = PredictionBoxModel(x = 40.0 + position * 180.0, y = 60.0 + line * 110.0, w = 160.0, h = 60.0),
    line = line,
    gloss = PredictionGlossModel(lemma = text, translation = translation, definition = "noun: $translation"),
    lineTranslation = lineTranslation,
)

private val previewWords = listOf(
    previewWord("Ἐπιλύσεις", "solutions", line = 0, position = 0, lineTranslation = "Short solutions and"),
    previewWord("σύντομοι", "short", line = 0, position = 1, lineTranslation = "Short solutions and"),
    previewWord("καὶ", "and", line = 0, position = 2, lineTranslation = "Short solutions and"),
    previewWord("ἐξηγήσεις", "explanations", line = 1, position = 0),
    previewWord("ἀποριῶν", "difficult questions", line = 1, position = 1, confirmed = false),
)

internal const val PREVIEW_UNCONFIRMED_WORD = 4

internal val previewTranslatedPrediction = PredictionModel(
    predictions = previewWords,
    modelVersion = "claude-opus-5-5+kraken-kraken_eparchos",
)

internal val previewUntranslatedPrediction = PredictionModel(
    predictions = previewWords.map { it.copy(gloss = PredictionGlossModel("", "", ""), lineTranslation = null) },
    modelVersion = "kraken_eparchos",
)

internal val previewEmptyPrediction = PredictionModel(predictions = emptyList(), modelVersion = "mock")

internal fun previewPage(): ImageBitmap {
    val image = ImageBitmap(600, 300)
    Canvas(image).drawRect(Rect(0f, 0f, 600f, 300f), Paint().apply { color = Color(0xFFE9DCC0) })
    return image
}
