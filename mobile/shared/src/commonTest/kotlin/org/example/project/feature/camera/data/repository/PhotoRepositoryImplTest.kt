package org.example.project.feature.camera.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.example.project.feature.camera.data.network.PhotoUploadApi
import org.example.project.feature.camera.data.network.PhotoUploadConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class PhotoRepositoryImplTest {
    private val config = PhotoUploadConfig(
        baseUrl = "http://localhost:8000/",
        uploadPath = "/predict",
        multipartFieldName = "image",
    )

    @Test
    fun uploadPhotoPreservesWordWhenGlossIsNull() = runTest {
        val api = apiWithJson(
            """{"predictions":[{"text":"logos","probabilities":{"logos":1.0},"box":{"x":10,"y":20,"w":30,"h":40},"line":2,"gloss":null}],"model_version":"v1"}"""
        )
        val repository = PhotoRepositoryImpl(api)

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
        val repository = PhotoRepositoryImpl(api)

        val result = repository.uploadPhoto(byteArrayOf(1, 2, 3))
        val word = result.predictions.single()

        assertEquals("logos", word.text)
        assertEquals("", word.gloss.lemma)
        assertEquals(" ", word.gloss.translation)
        assertEquals("", word.gloss.definition)
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

