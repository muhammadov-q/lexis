package org.example.project.common.presentation.helpers

import org.example.project.common.presentation.models.PredictionBoxModel
import org.example.project.common.presentation.models.PredictionGlossModel
import org.example.project.common.presentation.models.WordPredictionModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReadingHelpersTest {
    private fun word(
        text: String,
        line: Int,
        box: PredictionBoxModel = PredictionBoxModel(0.0, 0.0, 10.0, 10.0),
        gloss: PredictionGlossModel = PredictionGlossModel("", "", ""),
        probabilities: Map<String, Double> = mapOf(text to 0.9),
        lineTranslation: String? = null,
    ) = WordPredictionModel(text, probabilities, box, line, gloss, lineTranslation)

    @Test
    fun positionCountsWordsWithinTheLineFromOne() {
        val words = listOf(word("a", 0), word("b", 0), word("c", 1), word("d", 1))

        assertEquals(WordPosition(line = 1, word = 2), positionOf(words, 1))
        assertEquals(WordPosition(line = 2, word = 2), positionOf(words, 3))
    }

    @Test
    fun confidenceIsNullWhenKrakenDidNotConfirmTheWord() {
        assertEquals(0.9, word("a", 0).confidence())
        assertNull(word("a", 0, probabilities = mapOf("b" to 0.9)).confidence())
    }

    @Test
    fun translatedWhenAWordHasAGlossOrALineHasATranslation() {
        assertFalse(listOf(word("a", 0)).isTranslated())
        assertTrue(listOf(word("a", 0, gloss = PredictionGlossModel("", "and", ""))).isTranslated())
        assertTrue(listOf(word("a", 0, lineTranslation = "and so")).isTranslated())
    }

    @Test
    fun formatsLikeTheWebApp() {
        assertEquals("850 ms", formatDuration(850))
        assertEquals("68.6 s", formatDuration(68_600))
        assertEquals("1.0 s", formatDuration(1_000))
        assertEquals("95%", formatPercent(0.953))
    }

    @Test
    fun recognisesPlaceholderModels() {
        assertTrue(isPlaceholderModel("mock"))
        assertTrue(isPlaceholderModel("eparchos-knn"))
        assertFalse(isPlaceholderModel("claude-opus-5-5+kraken-kraken_eparchos"))
    }

    @Test
    fun aWordHasAGlossWhenAnyGlossFieldIsFilled() {
        assertFalse(word("a", 0).hasGloss())
        assertFalse(word("a", 0, gloss = PredictionGlossModel(" ", "", "")).hasGloss())
        assertTrue(word("a", 0, gloss = PredictionGlossModel("", "", "a definition")).hasGloss())
    }

    @Test
    fun formatsDurationsJustBelowAndAboveOneSecond() {
        assertEquals("999 ms", formatDuration(999))
        assertEquals("1.0 s", formatDuration(1_049))
        assertEquals("1.1 s", formatDuration(1_050))
    }

    @Test
    fun formatsWholeAndZeroPercent() {
        assertEquals("100%", formatPercent(1.0))
        assertEquals("0%", formatPercent(0.0))
    }

    @Test
    fun loupeCropKeepsTheAspectAndStaysOnThePage() {
        val crop = loupeCrop(PredictionBoxModel(0.0, 0.0, 30.0, 10.0), imageWidth = 1000, imageHeight = 500)

        assertEquals(0.0, crop.x)
        assertEquals(0.0, crop.y)
        assertEquals(3.0, crop.w / crop.h, 1e-9)
    }

    @Test
    fun loupeCropStaysOnThePageAtTheBottomRightCorner() {
        val crop = loupeCrop(PredictionBoxModel(970.0, 490.0, 30.0, 10.0), imageWidth = 1000, imageHeight = 500)

        assertTrue(crop.x + crop.w <= 1000.0)
        assertTrue(crop.y + crop.h <= 500.0)
    }

    @Test
    fun lineCentersSitHalfwayDownEachLineAndSkipEmptyBoxes() {
        val words = listOf(
            word("a", 0, PredictionBoxModel(0.0, 10.0, 5.0, 10.0)),
            word("b", 0, PredictionBoxModel(10.0, 5.0, 5.0, 20.0)),
            word("c", 1, PredictionBoxModel(0.0, 0.0, 0.0, 0.0)),
        )

        assertEquals(mapOf(0 to 15.0), lineCenters(words))
    }
}
