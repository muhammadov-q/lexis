package org.example.project.feature.camera.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.example.project.core.persistence.SessionCookieStore
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.camera.data.network.PhotoUploadApi
import org.example.project.feature.camera.data.network.PhotoUploadConfig
import org.example.project.feature.history.domain.session.HistoryRefreshStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PhotoRepositoryImplTest {
    private val config = PhotoUploadConfig(
        baseUrl = "http://localhost:8000/",
        guestUploadPath = "/predict",
        authenticatedUploadPath = "/recognitions",
        multipartFieldName = "image",
    )

    @Test
    fun uploadPhotoPreservesWordWhenGlossIsNull() = runTest {
        val api = apiWithJson(
            """{"predictions":[{"text":"logos","probabilities":{"logos":1.0},"box":{"x":10,"y":20,"w":30,"h":40},"line":2,"gloss":null}],"model_version":"v1"}"""
        )
        val repository = PhotoRepositoryImpl(api, config, AuthSessionStore(), FakeSessionCookieStore(), HistoryRefreshStore())

        val result = repository.uploadPhoto(byteArrayOf(1, 2, 3))
        val word = result.predictions.single()

        assertEquals("logos", word.text)
        assertEquals("", word.gloss.translation)
        assertEquals("", word.gloss.lemma)
        assertEquals("", word.gloss.definition)
    }

    @Test
    fun uploadPhotoPreservesBlankGlossFields() = runTest {
        val api = apiWithJson(
            """{"predictions":[{"text":"logos","probabilities":{"logos":1.0},"box":{"x":10,"y":20,"w":30,"h":40},"line":2,"gloss":{"lemma":"","translation":" ","definition":""}}],"model_version":"v1"}"""
        )
        val repository = PhotoRepositoryImpl(api, config, AuthSessionStore(), FakeSessionCookieStore(), HistoryRefreshStore())

        val result = repository.uploadPhoto(byteArrayOf(1, 2, 3))
        val word = result.predictions.single()

        assertEquals("logos", word.text)
        assertEquals("", word.gloss.lemma)
        assertEquals(" ", word.gloss.translation)
        assertEquals("", word.gloss.definition)
    }

    @Test
    fun uploadPhotoUsesRecognitionsEndpointWhenAuthenticated() = runTest {
        val authSessionStore = AuthSessionStore().apply {
            setAuthenticated(User(1, "user@example.com", "John", "Doe"))
        }
        var requestPath = ""
        val engine = MockEngine { request ->
            requestPath = request.url.encodedPath
            respond(
                content = """{"predictions":[],"model_version":"v1"}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val repository = PhotoRepositoryImpl(
            api = PhotoUploadApi(HttpClient(engine), config),
            config = config,
            authSessionStore = authSessionStore,
            sessionCookieStore = FakeSessionCookieStore(),
            historyRefreshStore = HistoryRefreshStore(),
        )

        repository.uploadPhoto(byteArrayOf(1))

        assertEquals("/recognitions", requestPath)
    }

    @Test
    fun uploadPhotoFallsBackToPredictWhenAuthenticatedSessionExpired() = runTest {
        val authSessionStore = AuthSessionStore().apply {
            setAuthenticated(User(1, "user@example.com", "John", "Doe"))
        }
        val cookieStore = FakeSessionCookieStore()
        val refreshStore = HistoryRefreshStore()
        val paths = mutableListOf<String>()
        val engine = MockEngine { request: HttpRequestData ->
            paths += request.url.encodedPath
            if (request.url.encodedPath == "/recognitions") {
                respond(
                    content = """{"detail":"expired"}""",
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else {
                respond(
                    content = """{"predictions":[],"model_version":"v1"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }

        val repository = PhotoRepositoryImpl(
            api = PhotoUploadApi(HttpClient(engine), config),
            config = config,
            authSessionStore = authSessionStore,
            sessionCookieStore = cookieStore,
            historyRefreshStore = refreshStore,
        )

        repository.uploadPhoto(byteArrayOf(1))

        assertEquals(listOf("/recognitions", "/predict"), paths)
        assertFalse(authSessionStore.isAuthenticated())
        assertEquals(1, cookieStore.clearCalls)
        assertEquals(0L, refreshStore.refreshVersion.value)
    }

    private fun apiWithJson(responseJson: String): PhotoUploadApi {
        val engine = MockEngine {
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        return PhotoUploadApi(HttpClient(engine), config)
    }
}

private class FakeSessionCookieStore : SessionCookieStore {
    var clearCalls = 0

    override fun clearSessionCookie() {
        clearCalls += 1
    }
}

