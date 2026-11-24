package org.example.project.feature.vocabulary.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.example.project.feature.vocabulary.domain.model.VocabularyWord

interface VocabularyRepository {
    val words: StateFlow<List<VocabularyWord>?>

    suspend fun refresh(): List<VocabularyWord>
    suspend fun get(id: Long): VocabularyWord
    suspend fun add(recognitionId: Long, wordIndex: Int, note: String): VocabularyWord
    suspend fun updateNote(id: Long, note: String): VocabularyWord
    suspend fun remove(id: Long)
}
