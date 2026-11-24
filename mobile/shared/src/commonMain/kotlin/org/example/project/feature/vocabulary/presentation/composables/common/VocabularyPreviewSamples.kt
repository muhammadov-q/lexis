package org.example.project.feature.vocabulary.presentation.composables.common

import org.example.project.feature.vocabulary.domain.model.VocabularyGloss
import org.example.project.feature.vocabulary.domain.model.VocabularyWord
import org.example.project.feature.vocabulary.domain.model.WordSource

internal val previewVocabularyWord = VocabularyWord(
    id = 1,
    createdAt = "2026-11-24T10:30:00Z",
    word = "λόγος",
    gloss = VocabularyGloss("λόγος", "word", "A word, speech or account."),
    note = "Also means reason.",
    source = WordSource(recognitionId = 7, filename = "6791 (1).png", line = 2, wordIndex = 14),
)

internal val previewUntranslatedWord = VocabularyWord(
    id = 2,
    createdAt = "2026-11-24T10:31:00Z",
    word = "ἐστίν",
    gloss = null,
    note = "",
    source = WordSource(recognitionId = 7, filename = "", line = 0, wordIndex = 1),
)

internal val previewVocabularyWords = listOf(previewVocabularyWord, previewUntranslatedWord)
