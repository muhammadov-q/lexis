package org.example.project.feature.vocabulary.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class HeadwordTest {
    @Test
    fun dropsPunctuationAroundTheWord() {
        assertEquals("ἐστί", headword("ἐστί,"))
        assertEquals("λόγος", headword("(λόγος)"))
        assertEquals("ἀλλά", headword(" ἀλλά· "))
        assertEquals("θεός", headword("«θεός»;"))
    }

    @Test
    fun keepsPunctuationInsideTheWord() {
        assertEquals("δ’ἄν", headword("δ’ἄν."))
    }

    @Test
    fun punctuationAloneIsNoWord() {
        assertEquals("", headword("·"))
        assertEquals("", headword(" . "))
    }
}
