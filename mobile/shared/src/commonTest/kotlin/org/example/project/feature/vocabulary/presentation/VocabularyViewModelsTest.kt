package org.example.project.feature.vocabulary.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_error_unauthorized
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.core.network.ApiException
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.vocabulary.domain.model.VocabularyWord
import org.example.project.feature.vocabulary.domain.model.WordSource
import org.example.project.feature.vocabulary.domain.repository.VocabularyRepository
import org.example.project.feature.vocabulary.presentation.list.VocabularyEvent
import org.example.project.feature.vocabulary.presentation.list.VocabularyViewModel
import org.example.project.feature.vocabulary.presentation.note.WordNoteEvent
import org.example.project.feature.vocabulary.presentation.note.WordNoteViewModel
import org.example.project.feature.vocabulary.presentation.word.VocabularyWordEffect
import org.example.project.feature.vocabulary.presentation.word.VocabularyWordEvent
import org.example.project.feature.vocabulary.presentation.word.VocabularyWordViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VocabularyViewModelsTest {
    @Test
    fun savingANewWordAddsItWithTheNote() = runVocabularyTest {
        val repository = FakeVocabularyRepository()
        val viewModel = WordNoteViewModel(repository, signedIn())

        viewModel.onEvent(WordNoteEvent.LoadRequested)
        advanceUntilIdle()
        viewModel.onEvent(WordNoteEvent.SaveRequested(7, 3, "λόγος", "  a note  "))
        advanceUntilIdle()

        assertEquals(listOf(Triple(7L, 3, "a note")), repository.added)
        assertEquals("λόγος", viewModel.state.value.savedWord)
        assertEquals(listOf("λόγος"), viewModel.state.value.words?.map { it.word })
    }

    @Test
    fun savingAWordAlreadyInTheVocabularyUpdatesItsNote() = runVocabularyTest {
        val repository = FakeVocabularyRepository(listOf(word(id = 4, text = "λόγος")))
        val viewModel = WordNoteViewModel(repository, signedIn())

        viewModel.onEvent(WordNoteEvent.LoadRequested)
        advanceUntilIdle()
        viewModel.onEvent(WordNoteEvent.SaveRequested(9, 0, "λόγος", "new"))
        advanceUntilIdle()

        assertTrue(repository.added.isEmpty())
        assertEquals(listOf(4L to "new"), repository.notes)
    }

    @Test
    fun aWordSavedElsewhereMeanwhileGetsTheNote() = runVocabularyTest {
        val repository = FakeVocabularyRepository(
            serverWords = listOf(word(id = 5, text = "λόγος")),
            addError = ApiException(409),
        )
        val viewModel = WordNoteViewModel(repository, signedIn())
        repository.setLoaded(emptyList())

        viewModel.onEvent(WordNoteEvent.SaveRequested(7, 3, "λόγος", "note"))
        advanceUntilIdle()

        assertEquals(listOf(5L to "note"), repository.notes)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun theListAsksToSignInOn401() = runVocabularyTest {
        val repository = FakeVocabularyRepository(refreshError = ApiException(401))
        val viewModel = VocabularyViewModel(repository, signedIn())

        viewModel.onEvent(VocabularyEvent.LoadRequested)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.requiresSignIn)
        assertEquals(UiText.Resource(Res.string.vocabulary_error_unauthorized), viewModel.state.value.error)
    }

    @Test
    fun signingOutForgetsTheWords() = runVocabularyTest {
        val session = signedIn()
        val repository = FakeVocabularyRepository(listOf(word(id = 1, text = "λόγος")))
        val viewModel = VocabularyViewModel(repository, session)
        viewModel.onEvent(VocabularyEvent.LoadRequested)
        advanceUntilIdle()

        session.setGuest()
        advanceUntilIdle()

        assertNull(viewModel.state.value.words)
    }

    @Test
    fun theNoteFieldRefreshesWordsItAlreadyHas() = runVocabularyTest {
        val repository = FakeVocabularyRepository(listOf(word(id = 6, text = "θεός")))
        repository.setLoaded(listOf(word(id = 1, text = "λόγος")))
        val viewModel = WordNoteViewModel(repository, signedIn())

        viewModel.onEvent(WordNoteEvent.LoadRequested)
        advanceUntilIdle()

        assertEquals(listOf("θεός"), viewModel.state.value.words?.map { it.word })
    }

    @Test
    fun aWordCanBeEditedAndRemoved() = runVocabularyTest {
        val repository = FakeVocabularyRepository(listOf(word(id = 2, text = "θεός", note = "old")))
        val viewModel = VocabularyWordViewModel(repository, signedIn())

        viewModel.onEvent(VocabularyWordEvent.LoadRequested(2))
        advanceUntilIdle()
        assertEquals("θεός", viewModel.state.value.word?.word)

        viewModel.onEvent(VocabularyWordEvent.SaveNoteRequested("new "))
        advanceUntilIdle()
        assertEquals("new", viewModel.state.value.word?.note)
        assertTrue(viewModel.state.value.isNoteSaved)

        viewModel.onEvent(VocabularyWordEvent.RemoveRequested)
        assertTrue(viewModel.state.value.isRemoveDialogShown)
        viewModel.onEvent(VocabularyWordEvent.RemoveConfirmed)
        advanceUntilIdle()

        assertEquals(VocabularyWordEffect.Removed("θεός"), viewModel.effects.first())
        assertEquals(listOf(2L), repository.removed)
        assertFalse(viewModel.state.value.isRemoveDialogShown)
    }

    private fun signedIn() = AuthSessionStore().apply {
        setAuthenticated(User(id = 1, email = "reader@example.com", firstName = null, lastName = null))
    }

    private fun runVocabularyTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private fun word(id: Long, text: String, note: String = "") = VocabularyWord(
    id = id,
    createdAt = "2026-11-24T10:00:00Z",
    word = text,
    gloss = null,
    note = note,
    source = WordSource(recognitionId = 7, filename = "page.png", line = 0, wordIndex = 0),
)

private class FakeVocabularyRepository(
    private var serverWords: List<VocabularyWord> = emptyList(),
    private val refreshError: Exception? = null,
    private val addError: Exception? = null,
) : VocabularyRepository {
    private val _words = MutableStateFlow<List<VocabularyWord>?>(null)
    override val words: StateFlow<List<VocabularyWord>?> = _words

    val added = mutableListOf<Triple<Long, Int, String>>()
    val notes = mutableListOf<Pair<Long, String>>()
    val removed = mutableListOf<Long>()

    fun setLoaded(words: List<VocabularyWord>) {
        _words.value = words
    }

    override suspend fun refresh(): List<VocabularyWord> {
        refreshError?.let { throw it }
        _words.value = serverWords
        return serverWords
    }

    override suspend fun get(id: Long): VocabularyWord = serverWords.first { it.id == id }

    override suspend fun add(recognitionId: Long, wordIndex: Int, note: String): VocabularyWord {
        addError?.let { throw it }
        added += Triple(recognitionId, wordIndex, note)
        val saved = word(id = 100L + added.size, text = "λόγος", note = note)
        serverWords = listOf(saved) + serverWords
        _words.value = serverWords
        return saved
    }

    override suspend fun updateNote(id: Long, note: String): VocabularyWord {
        notes += id to note
        val saved = serverWords.first { it.id == id }.copy(note = note)
        serverWords = serverWords.map { if (it.id == id) saved else it }
        _words.value = serverWords
        return saved
    }

    override suspend fun remove(id: Long) {
        removed += id
        serverWords = serverWords.filter { it.id != id }
        _words.value = serverWords
    }
}
