package org.example.project.feature.camera.data.repository

import org.example.project.core.network.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.example.project.core.network.platform.handleUnauthorizedResponses
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.camera.data.network.PhotoUploadApi
import org.example.project.feature.camera.data.network.PhotoUploadConfig
import org.example.project.feature.camera.domain.model.OcrModel
import org.example.project.feature.camera.domain.model.OcrModels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhotoRepositoryImplTest {
    private val apiConfig = ApiConfig(baseUrl = "http://localhost:8000/")
    private val config = PhotoUploadConfig(
        guestUploadPath = "/predict",
        authenticatedUploadPath = "/recognitions",
        multipartFieldName = "image",
    )

    @Test
    fun getModelsMapsModelsAndDefaultModelFromApi() = runTest {
        val api = apiWithJson(
            """{"models":[{"id":"one","name":"First","description":"First model"},{"id":"two","name":"Second","description":"Second model"}],"default_model":"two"}"""
        )
        val repository = PhotoRepositoryImpl(api, config, AuthSessionStore())

        val result = repository.getModels()

        assertEquals(
            OcrModels(
                models = listOf(
                    OcrModel("one", "First", "First model"),
                    OcrModel("two", "Second", "Second model"),
                ),
                defaultModel = "two",
            ),
            result,
        )
    }

    @Test
    fun uploadPhotoPreservesWordWhenGlossIsNull() = runTest {
        val api = apiWithJson(
            """{"predictions":[{"text":"logos","probabilities":{"logos":1.0},"box":{"x":10,"y":20,"w":30,"h":40},"line":2,"gloss":null}],"model_version":"v1"}"""
        )
        val repository = PhotoRepositoryImpl(api, config, AuthSessionStore())

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
        val repository = PhotoRepositoryImpl(api, config, AuthSessionStore())

        val result = repository.uploadPhoto(byteArrayOf(1, 2, 3))
        val word = result.predictions.single()

        assertEquals("logos", word.text)
        assertEquals("", word.gloss.lemma)
        assertEquals(" ", word.gloss.translation)
        assertEquals("", word.gloss.definition)
    }

    @Test
    fun uploadPhotoMapsUnconfirmedWordsAndOptionalFieldsInOrder() = runTest {
        val repository = PhotoRepositoryImpl(
            apiWithJson(
                """{"model_version":"reader+kraken","predictions":[
                    {"text":"λόγος","probabilities":{},"box":{"x":1,"y":2,"w":3,"h":4},"line":2,"gloss":{"lemma":null,"translation":"word"},"line_translation":"A line."},
                    {"text":"?","probabilities":{"other":0.8},"box":{"x":5,"y":6,"w":7,"h":8}}
                ]}"""
            ),
            config,
            AuthSessionStore(),
        )

        val result = repository.uploadPhoto(byteArrayOf(1))

        assertEquals("reader+kraken", result.modelVersion)
        assertEquals(listOf("λόγος", "?"), result.predictions.map { it.text })
        val first = result.predictions[0]
        assertTrue(first.probabilities.isEmpty())
        assertEquals(2, first.line)
        assertEquals(3.0, first.box.w)
        assertEquals("", first.gloss.lemma)
        assertEquals("word", first.gloss.translation)
        assertEquals("", first.gloss.definition)
        assertEquals("A line.", first.lineTranslation)
        assertEquals(mapOf("other" to 0.8), result.predictions[1].probabilities)
        assertEquals(0, result.predictions[1].line)
        assertEquals(null, result.predictions[1].lineTranslation)
        assertEquals("", result.predictions[1].gloss.translation)
    }

    @Test
    fun uploadPhotoMapsEmptyPredictions() = runTest {
        val repository = PhotoRepositoryImpl(
            apiWithJson("""{"predictions":[],"model_version":"mock"}"""),
            config,
            AuthSessionStore(),
        )

        assertTrue(repository.uploadPhoto(byteArrayOf(1)).predictions.isEmpty())
    }

    @Test
    fun uploadPhotoKeepsTheSavedIdFromRecognitions() = runTest {
        val authSessionStore = AuthSessionStore().apply {
            setAuthenticated(User(1, "user@example.com", "John", "Doe"))
        }
        val engine = MockEngine {
            respond(
                content = """{"id":42,"created_at":"2026-11-20T20:45:00Z","filename":"page.jpg","predictions":[],"model_version":"v1"}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val repository = PhotoRepositoryImpl(
            api = PhotoUploadApi(HttpClient(engine), apiConfig, config),
            config = config,
            authSessionStore = authSessionStore,
        )

        val prediction = repository.uploadPhoto(byteArrayOf(1))

        assertEquals(42L, prediction.savedId)
    }

    @Test
    fun uploadPhotoHasNoSavedIdForGuests() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"predictions":[],"model_version":"v1"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val repository = PhotoRepositoryImpl(
            api = PhotoUploadApi(HttpClient(engine), apiConfig, config),
            config = config,
            authSessionStore = AuthSessionStore(),
        )

        val prediction = repository.uploadPhoto(byteArrayOf(1))

        assertNull(prediction.savedId)
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
            api = PhotoUploadApi(HttpClient(engine), apiConfig, config),
            config = config,
            authSessionStore = authSessionStore,
        )

        repository.uploadPhoto(byteArrayOf(1))

        assertEquals("/recognitions", requestPath)
    }

    @Test
    fun uploadPhotoFallsBackToPredictWhenAuthenticatedSessionExpired() = runTest {
        val authSessionStore = AuthSessionStore().apply {
            setAuthenticated(User(1, "user@example.com", "John", "Doe"))
        }
        val paths = mutableListOf<String>()
        val bodies = mutableListOf<String>()
        val engine = MockEngine { request: HttpRequestData ->
            paths += request.url.encodedPath
            bodies += request.body.toByteArray().decodeToString()
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
            api = PhotoUploadApi(
                HttpClient(engine) {
                    handleUnauthorizedResponses(authSessionStore)
                },
                apiConfig,
                config,
            ),
            config = config,
            authSessionStore = authSessionStore,
        )

        repository.uploadPhoto(byteArrayOf(1), "chosen")

        assertEquals(listOf("/recognitions", "/predict"), paths)
        assertTrue(bodies.all { it.contains("name=model") && it.contains("chosen") })
        assertFalse(authSessionStore.isAuthenticated())
    }

    private fun apiWithJson(responseJson: String): PhotoUploadApi {
        val engine = MockEngine {
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        return PhotoUploadApi(HttpClient(engine), apiConfig, config)
    }
}
