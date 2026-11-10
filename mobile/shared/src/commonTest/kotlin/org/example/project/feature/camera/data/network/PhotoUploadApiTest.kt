package org.example.project.feature.camera.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.example.project.feature.camera.domain.model.ApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PhotoUploadApiTest {
    private val config = PhotoUploadConfig(
        baseUrl = "http://localhost:8000/",
        guestUploadPath = "/predict",
        authenticatedUploadPath = "/recognitions",
        multipartFieldName = "image",
    )
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun api(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        PhotoUploadApi(HttpClient(MockEngine(handler)), config)

    @Test
    fun successfulUploadParsesThePrediction() = runTest {
        val api = api {
            respond(
                """{"predictions":[{"text":"λόγος","probabilities":{"λόγος":0.96,"λόγον":0.04},"box":{"x":1,"y":2,"w":3,"h":4},"line":0,"gloss":{"lemma":"λόγος","translation":"word","definition":"A word."}}],"model_version":"v1"}""",
                HttpStatusCode.OK,
                jsonHeaders,
            )
        }

        val result = api.upload(byteArrayOf(1, 2, 3), "/predict")

        assertEquals("v1", result.modelVersion)
        assertEquals("λόγος", result.predictions.single().text)
        assertEquals(0, result.predictions.single().line)
        assertEquals("word", result.predictions.single().gloss?.translation)
    }

    @Test
    fun uploadPostsMultipartImageFieldToPredict() = runTest {
        var method: HttpMethod? = null
        var url: String? = null
        var body = ""
        val api = api { request ->
            method = request.method
            url = request.url.toString()
            body = request.body.toByteArray().decodeToString()
            respond("""{"predictions":[],"model_version":"v1"}""", HttpStatusCode.OK, jsonHeaders)
        }

        api.upload(byteArrayOf(1), "/predict")

        assertEquals(HttpMethod.Post, method)
        assertEquals("http://localhost:8000/predict", url)
        assertTrue(body.contains("name=image"), body)
        assertFalse(body.contains("name=model"), body)
    }

    @Test
    fun modelsAreFetchedAndParsed() = runTest {
        val api = api { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/models", request.url.encodedPath)
            respond(
                """{"models":[{"id":"one","name":"First","description":"Description"}],"default_model":"one"}""",
                HttpStatusCode.OK, jsonHeaders,
            )
        }
        val result = api.getModels()
        assertEquals("one", result.defaultModel)
        assertEquals("Description", result.models.single().description)
    }

    @Test
    fun modelIsSentForGuestAndAuthenticatedRequests() = runTest {
        val paths = mutableListOf<String>()
        val api = api { request ->
            paths += request.url.encodedPath
            val body = request.body.toByteArray().decodeToString()
            assertTrue(body.contains("name=image"), body)
            assertTrue(body.contains("name=model"), body)
            assertTrue(body.contains("chosen-model"), body)
            respond("""{"predictions":[],"model_version":"v1"}""", HttpStatusCode.OK, jsonHeaders)
        }
        api.upload(byteArrayOf(1), "/predict", "chosen-model")
        api.upload(byteArrayOf(1), "/recognitions", "chosen-model")
        assertEquals(listOf("/predict", "/recognitions"), paths)
    }

    @Test
    fun modelDiscoveryHttpErrorIsReported() = runTest {
        val api = api { respond("", HttpStatusCode.ServiceUnavailable) }
        assertEquals(503, assertFailsWith<ApiException> { api.getModels() }.status)
    }

    @Test
    fun modelDiscoveryConnectionFailureThrowsApiExceptionWithStatusZero() = runTest {
        val api = api { throw RuntimeException("connection refused") }

        val error = assertFailsWith<ApiException> { api.getModels() }

        assertEquals(0, error.status)
    }

    @Test
    fun malformedModelDiscoveryBodyThrowsSerializationException() = runTest {
        val api = api { respond("not json", HttpStatusCode.OK, jsonHeaders) }

        assertFailsWith<SerializationException> { api.getModels() }
    }

    @Test
    fun modelDiscoveryCancellationIsNotConvertedToApiException() = runTest {
        val api = api { throw CancellationException("cancelled") }

        assertFailsWith<CancellationException> { api.getModels() }
    }

    @Test
    fun uploadUsesJpegMimeTypeAndFilename() = runTest {
        var body = ""
        val api = api { request ->
            body = request.body.toByteArray().decodeToString()
            respond("""{"predictions":[],"model_version":"v1"}""", HttpStatusCode.OK, jsonHeaders)
        }

        api.upload(byteArrayOf(1, 2, 3), "/predict")

        assertTrue(body.contains("image/jpeg"), body)
        assertTrue(body.contains("filename=\"photo.jpg\""), body)
    }

    @Test
    fun successfulUploadAllowsNullGloss() = runTest {
        val api = api {
            respond(
                """{"predictions":[{"text":"λόγος","probabilities":{"λόγος":1.0},"box":{"x":1,"y":2,"w":3,"h":4},"line":0,"gloss":null}],"model_version":"v1"}""",
                HttpStatusCode.OK,
                jsonHeaders,
            )
        }

        val result = api.upload(byteArrayOf(1, 2, 3), "/predict")

        assertEquals("λόγος", result.predictions.single().text)
        assertEquals(null, result.predictions.single().gloss)
    }

    @Test
    fun successfulUploadAllowsNullGlossFields() = runTest {
        val api = api {
            respond(
                """{"predictions":[{"text":"λόγος","probabilities":{"λόγος":1.0},"box":{"x":1,"y":2,"w":3,"h":4},"line":0,"gloss":{"lemma":null,"translation":null,"definition":null}}],"model_version":"v1"}""",
                HttpStatusCode.OK,
                jsonHeaders,
            )
        }

        val result = api.upload(byteArrayOf(1, 2, 3), "/predict")
        val gloss = result.predictions.single().gloss

        assertEquals("λόγος", result.predictions.single().text)
        assertEquals(null, gloss?.lemma)
        assertEquals(null, gloss?.translation)
        assertEquals(null, gloss?.definition)
    }

    @Test
    fun nonOkStatusesThrowApiExceptionWithThatStatus() = runTest {
        listOf(400, 413, 415, 422, 500, 502).forEach { status ->
            val api = api { respond("""{"detail":"nope"}""", HttpStatusCode.fromValue(status), jsonHeaders) }

            val error = assertFailsWith<ApiException>("status $status") { api.upload(byteArrayOf(1), "/predict") }

            assertEquals(status, error.status)
        }
    }

    @Test
    fun nonOkStatusWithoutBodyStillThrowsApiException() = runTest {
        val api = api { respond("", HttpStatusCode.InternalServerError) }

        val error = assertFailsWith<ApiException> { api.upload(byteArrayOf(1), "/predict") }

        assertEquals(500, error.status)
    }

    @Test
    fun connectionFailureThrowsApiExceptionWithStatusZero() = runTest {
        val api = api { throw RuntimeException("connection refused") }

        val error = assertFailsWith<ApiException> { api.upload(byteArrayOf(1), "/predict") }

        assertEquals(0, error.status)
    }

    @Test
    fun malformedSuccessBodyThrowsSerializationException() = runTest {
        val api = api { respond("not json", HttpStatusCode.OK, jsonHeaders) }

        assertFailsWith<SerializationException> { api.upload(byteArrayOf(1), "/predict") }
    }

    @Test
    fun cancellationIsNotConvertedToApiException() = runTest {
        val api = api { throw CancellationException("cancelled") }

        assertFailsWith<CancellationException> { api.upload(byteArrayOf(1), "/predict") }
    }
}
