package org.example.project.feature.camera.presentation.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.example.project.feature.camera.presentation.helpers.groupPredictionLines
import org.example.project.feature.camera.presentation.helpers.isConfirmed

class PredictionLinesTest {
    private fun word(
        text: String,
        line: Int,
        translation: String? = null,
        probabilities: Map<String, Double> = emptyMap(),
    ) = WordPredictionModel(
        text = text,
        probabilities = probabilities,
        box = PredictionBoxModel(1.0, 2.0, 3.0, 4.0),
        line = line,
        gloss = PredictionGlossModel("", "", ""),
        lineTranslation = translation,
    )

    @Test
    fun groupsLinesInIncomingOrderAndKeepsSelectionIndices() {
        val lines = groupPredictionLines(
            listOf(
                word("one", 0),
                word("two", 0, "A line."),
                word("three", 1, "Another line."),
                word("four", 0, "A later line."),
            ),
        )

        assertEquals(listOf(0, 1, 0), lines.map { it.number })
        assertEquals(listOf("A line.", "Another line.", "A later line."), lines.map { it.translation })
        assertEquals(listOf(0, 1, 2, 3), lines.flatMap { it.words }.map { it.index })
        assertEquals(listOf("one", "two", "three", "four"), lines.flatMap { it.words }.map { it.value.text })
        assertTrue(groupPredictionLines(emptyList()).isEmpty())
    }

    @Test
    fun confirmationUsesTheRecognizedTextNotAnotherCandidate() {
        assertFalse(word("word", 0).isConfirmed())
        assertFalse(word("word", 0, probabilities = mapOf("other" to 0.9)).isConfirmed())
        assertTrue(word("word", 0, probabilities = mapOf("word" to 0.0)).isConfirmed())
    }
}
