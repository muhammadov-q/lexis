package org.example.project.feature.vocabulary.domain.model

data class VocabularyWord(
    val id: Long,
    val createdAt: String,
    val word: String,
    val gloss: VocabularyGloss?,
    val note: String,
    val source: WordSource,
)

data class VocabularyGloss(
    val lemma: String,
    val translation: String,
    val definition: String,
)

data class WordSource(
    val recognitionId: Long,
    val filename: String,
    val line: Int,
    val wordIndex: Int,
)

const val MAX_NOTE_LENGTH = 2000

fun headword(text: String): String = text.trim { it.isWhitespace() || it.isPunctuation() }

private fun Char.isPunctuation(): Boolean = when (category) {
    CharCategory.CONNECTOR_PUNCTUATION,
    CharCategory.DASH_PUNCTUATION,
    CharCategory.START_PUNCTUATION,
    CharCategory.END_PUNCTUATION,
    CharCategory.INITIAL_QUOTE_PUNCTUATION,
    CharCategory.FINAL_QUOTE_PUNCTUATION,
    CharCategory.OTHER_PUNCTUATION -> true
    else -> false
}
