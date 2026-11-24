package org.example.project.feature.vocabulary.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.feature.vocabulary.data.network.VocabularyApi
import org.example.project.feature.vocabulary.data.network.models.VocabularyWordDto
import org.example.project.feature.vocabulary.domain.model.VocabularyGloss
import org.example.project.feature.vocabulary.domain.model.VocabularyWord
import org.example.project.feature.vocabulary.domain.model.WordSource
import org.example.project.feature.vocabulary.domain.repository.VocabularyRepository

class VocabularyRepositoryImpl(
    private val api: VocabularyApi,
) : VocabularyRepository {
    private val _words = MutableStateFlow<List<VocabularyWord>?>(null)
    override val words: StateFlow<List<VocabularyWord>?> = _words.asStateFlow()

    override suspend fun refresh(): List<VocabularyWord> {
        val words = api.list().map { it.toDomain() }
        _words.value = words
        return words
    }

    override suspend fun get(id: Long): VocabularyWord {
        val word = api.get(id).toDomain()
        replace(word)
        return word
    }

    override suspend fun add(recognitionId: Long, wordIndex: Int, note: String): VocabularyWord {
        val word = api.add(recognitionId, wordIndex, note).toDomain()
        _words.update { words -> words?.let { listOf(word) + it.filter { saved -> saved.id != word.id } } }
        return word
    }

    override suspend fun updateNote(id: Long, note: String): VocabularyWord {
        val word = api.updateNote(id, note).toDomain()
        replace(word)
        return word
    }

    override suspend fun remove(id: Long) {
        api.remove(id)
        _words.update { words -> words?.filter { it.id != id } }
    }

    override fun clear() {
        _words.value = null
    }

    private fun replace(word: VocabularyWord) {
        _words.update { words -> words?.map { if (it.id == word.id) word else it } }
    }

    private fun VocabularyWordDto.toDomain() = VocabularyWord(
        id = id,
        createdAt = createdAt,
        word = word,
        gloss = gloss?.let {
            VocabularyGloss(
                lemma = it.lemma.orEmpty(),
                translation = it.translation.orEmpty(),
                definition = it.definition.orEmpty(),
            )
        },
        note = note,
        source = WordSource(
            recognitionId = source.recognitionId,
            filename = source.filename,
            line = source.line,
            wordIndex = source.wordIndex,
        ),
    )
}
