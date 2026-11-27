package org.example.project.feature.vocabulary.data.repository

import org.example.project.core.network.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.example.project.core.network.ApiException
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.vocabulary.data.network.VocabularyApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class VocabularyRepositoryImplTest {
    @Test
    fun refreshMapsWordsAndPublishesThem() = runTest {
        val repository = repository { respondJson(listOf(LOGOS, ESTIN).joinToString(",", "[", "]")) }

        val words = repository.refresh()

        assertEquals(listOf("λόγος", "ἐστίν"), words.map { it.word })
        assertEquals("word", words.first().gloss?.translation)
        assertNull(words.last().gloss)
        assertEquals(7L, words.first().source.recognitionId)
        assertEquals(14, words.first().source.wordIndex)
        assertEquals(words, repository.words.value)
    }

    @Test
    fun addSendsTheWordAndPutsItFirst() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val repository = repository { request ->
            requests += request
            when (request.method) {
                HttpMethod.Get -> respondJson("[$ESTIN]")
                else -> respondJson(LOGOS, HttpStatusCode.Created)
            }
        }
        repository.refresh()

        repository.add(recognitionId = 7, wordIndex = 14, note = "reason")

        val post = requests.last()
        assertEquals(HttpMethod.Post, post.method)
        assertEquals("/vocabulary", post.url.encodedPath)
        assertEquals(
            """{"recognition_id":7,"word_index":14,"note":"reason"}""",
            (post.body as TextContent).text,
        )
        assertEquals(listOf("λόγος", "ἐστίν"), repository.words.value?.map { it.word })
    }

    @Test
    fun updateNoteReplacesTheWord() = runTest {
        val repository = repository { request ->
            when (request.method) {
                HttpMethod.Get -> respondJson("[$LOGOS]")
                else -> respondJson(LOGOS.replace("\"note\":\"\"", "\"note\":\"new note\""))
            }
        }
        repository.refresh()

        repository.updateNote(1, "new note")

        assertEquals("new note", repository.words.value?.single()?.note)
    }

    @Test
    fun removeDropsTheWord() = runTest {
        val repository = repository { request ->
            when (request.method) {
                HttpMethod.Get -> respondJson("[$LOGOS,$ESTIN]")
                else -> respond("", HttpStatusCode.NoContent)
            }
        }
        repository.refresh()

        repository.remove(1)

        assertEquals(listOf("ἐστίν"), repository.words.value?.map { it.word })
    }

    @Test
    fun errorsCarryTheStatusAndDetail() = runTest {
        val repository = repository {
            respondJson("""{"detail":"This word is already in your vocabulary."}""", HttpStatusCode.Conflict)
        }

        val error = assertFailsWith<ApiException> { repository.add(7, 14, "") }

        assertEquals(409, error.status)
        assertEquals("This word is already in your vocabulary.", error.detail)
    }

    @Test
    fun signingOutForgetsTheWords() = runTest {
        val session = signedIn()
        val repository = repository(session) { respondJson("[$LOGOS]") }
        repository.refresh()

        session.setGuest()
        runCurrent()

        assertNull(repository.words.value)
    }

    @Test
    fun aListThatArrivesAfterSigningOutIsDropped() = runTest {
        val session = signedIn()
        val repository = repository(session) {
            session.setGuest()
            respondJson("[$LOGOS]")
        }

        repository.refresh()
        runCurrent()

        assertNull(repository.words.value)
    }

    private fun signedIn() = AuthSessionStore().apply {
        setAuthenticated(User(id = 1, email = "reader@example.com", firstName = null, lastName = null))
    }

    private fun TestScope.repository(
        session: AuthSessionStore = signedIn(),
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ) = VocabularyRepositoryImpl(
        api = VocabularyApi(HttpClient(MockEngine(handler)), ApiConfig("http://localhost:8000")),
        sessionStore = session,
        scope = backgroundScope,
    )

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )

    private companion object {
        const val LOGOS = """{"id":1,"created_at":"2026-11-24T10:30:00Z","word":"λόγος","gloss":{"lemma":"λόγος","translation":"word","definition":"A word."},"note":"","source":{"recognition_id":7,"filename":"page.png","line":2,"word_index":14}}"""
        const val ESTIN = """{"id":2,"created_at":"2026-11-24T10:00:00Z","word":"ἐστίν","gloss":null,"note":"check","source":{"recognition_id":7,"filename":"page.png","line":0,"word_index":1}}"""
    }
}
