package org.example.project.feature.camera.presentation.mappers

import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.PredictionBox
import org.example.project.feature.camera.domain.model.PredictionGloss
import org.example.project.feature.camera.domain.model.WordPrediction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PredictionMapperTest {
    @Test
    fun mapPredictionToUiModelMapsEveryField() {
        val domain = Prediction(
            predictions = listOf(
                WordPrediction(
                    text = "λόγος",
                    probabilities = mapOf("λόγος" to 0.4, "λόγον" to 0.6),
                    box = PredictionBox(x = 10.0, y = 20.0, w = 30.0, h = 40.0),
                    line = 2,
                    gloss = PredictionGloss(
                        lemma = "λόγος",
                        translation = "word",
                        definition = "A word or account.",
                    ),
                ),
            ),
            modelVersion = "0.1.0",
        )

        val ui = mapPredictionToUiModel(domain)
        val item = ui.predictions.single()

        assertEquals("0.1.0", ui.modelVersion)
        assertEquals("λόγος", item.text)
        assertEquals(mapOf("λόγος" to 0.4, "λόγον" to 0.6), item.probabilities)
        assertEquals(10.0, item.box.x)
        assertEquals(20.0, item.box.y)
        assertEquals(30.0, item.box.w)
        assertEquals(40.0, item.box.h)
        assertEquals(2, item.line)
        assertEquals("λόγος", item.gloss.lemma)
        assertEquals("word", item.gloss.translation)
        assertEquals("A word or account.", item.gloss.definition)
    }

    @Test
    fun mapPredictionToUiModelKeepsRecognizedTextWhenProbabilityTopKeyDiffers() {
        val domain = Prediction(
            predictions = listOf(
                WordPrediction(
                    text = "correct-text",
                    probabilities = mapOf("wrong-top" to 0.95, "correct-text" to 0.05),
                    box = PredictionBox(x = 0.0, y = 0.0, w = 1.0, h = 1.0),
                    line = 0,
                    gloss = PredictionGloss("lemma", "translation", "definition"),
                ),
            ),
            modelVersion = "v1",
        )

        val ui = mapPredictionToUiModel(domain)

        assertEquals("correct-text", ui.predictions.single().text)
    }

    @Test
    fun mapPredictionToUiModelHandlesEmptyPredictions() {
        val ui = mapPredictionToUiModel(Prediction(predictions = emptyList(), modelVersion = "v1"))

        assertTrue(ui.predictions.isEmpty())
        assertEquals("v1", ui.modelVersion)
    }

    @Test
    fun mapPredictionToUiModelPreservesIncomingOrderAndLineValues() {
        val domain = Prediction(
            predictions = listOf(
                WordPrediction("second", emptyMap(), PredictionBox(0.0, 0.0, 1.0, 1.0), line = 1, gloss = PredictionGloss("l2", "t2", "d2")),
                WordPrediction("first", emptyMap(), PredictionBox(0.0, 0.0, 1.0, 1.0), line = 0, gloss = PredictionGloss("l1", "t1", "d1")),
            ),
            modelVersion = "v1",
        )

        val ui = mapPredictionToUiModel(domain)

        assertEquals(listOf("second", "first"), ui.predictions.map { it.text })
        assertEquals(listOf(1, 0), ui.predictions.map { it.line })
    }

    @Test
    fun mapPredictionToUiModelPreservesBlankGlossValues() {
        val domain = Prediction(
            predictions = listOf(
                WordPrediction(
                    text = "logos",
                    probabilities = mapOf("logos" to 1.0),
                    box = PredictionBox(10.0, 20.0, 30.0, 40.0),
                    line = 0,
                    gloss = PredictionGloss(lemma = "", translation = " ", definition = ""),
                ),
            ),
            modelVersion = "v1",
        )

        val ui = mapPredictionToUiModel(domain)
        val word = ui.predictions.single()

        assertEquals("logos", word.text)
        assertEquals("", word.gloss.lemma)
        assertEquals(" ", word.gloss.translation)
        assertEquals("", word.gloss.definition)
    }
}

