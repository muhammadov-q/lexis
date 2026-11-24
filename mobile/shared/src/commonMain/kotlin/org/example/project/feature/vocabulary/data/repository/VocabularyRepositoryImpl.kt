package org.example.project.feature.vocabulary.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.session.SessionMode
import org.example.project.feature.vocabulary.data.network.VocabularyApi
import org.example.project.feature.vocabulary.data.network.models.VocabularyWordDto
import org.example.project.feature.vocabulary.domain.model.VocabularyGloss
import org.example.project.feature.vocabulary.domain.model.VocabularyWord
import org.example.project.feature.vocabulary.domain.model.WordSource
import org.example.project.feature.vocabulary.domain.repository.VocabularyRepository

class VocabularyRepositoryImpl(
    private val api: VocabularyApi,
    private val sessionStore: AuthSessionStore,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : VocabularyRepository {
    private val _words = MutableStateFlow<List<VocabularyWord>?>(null)
    override val words: StateFlow<List<VocabularyWord>?> = _words.asStateFlow()

    init {
        scope.launch {
            sessionStore.mode.collect { mode ->
                if (mode is SessionMode.Guest) _words.value = null
            }
        }
    }

    override suspend fun refresh(): List<VocabularyWord> {
        val session = sessionStore.mode.value
        val words = api.list().map { it.toDomain() }
        publish(session) { words }
        return words
    }

    override suspend fun get(id: Long): VocabularyWord {
        val session = sessionStore.mode.value
        val word = api.get(id).toDomain()
        publish(session) { words -> words?.replace(word) }
        return word
    }

    override suspend fun add(recognitionId: Long, wordIndex: Int, note: String): VocabularyWord {
        val session = sessionStore.mode.value
        val word = api.add(recognitionId, wordIndex, note).toDomain()
        publish(session) { words -> words?.let { listOf(word) + it.filter { saved -> saved.id != word.id } } }
        return word
    }

    override suspend fun updateNote(id: Long, note: String): VocabularyWord {
        val session = sessionStore.mode.value
        val word = api.updateNote(id, note).toDomain()
        publish(session) { words -> words?.replace(word) }
        return word
    }

    override suspend fun remove(id: Long) {
        val session = sessionStore.mode.value
        api.remove(id)
        publish(session) { words -> words?.filter { it.id != id } }
    }

    private fun publish(session: SessionMode, change: (List<VocabularyWord>?) -> List<VocabularyWord>?) {
        if (session is SessionMode.Authenticated && sessionStore.mode.value == session) _words.update(change)
    }

    private fun List<VocabularyWord>.replace(word: VocabularyWord) = map { if (it.id == word.id) word else it }

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
