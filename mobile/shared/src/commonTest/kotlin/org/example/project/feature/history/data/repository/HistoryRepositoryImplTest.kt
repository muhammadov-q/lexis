package org.example.project.feature.history.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.example.project.feature.auth.domain.model.AuthApiException
import org.example.project.feature.history.data.network.HistoryApi
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HistoryRepositoryImplTest {
    @Test
    fun getHistoryMapsPreviewTextFromPredictions() = runTest {
        val api = historyApi(
            listBody =
                """
                [
                  {
                    "id": 1,
                    "created_at": "2026-11-03T10:30:00Z",
                    "filename": "page.png",
                    "model_version": "eparchos",
                    "predictions": [
                      {"text":"λόγος","probabilities":{"λόγος":0.9},"box":{"x":1,"y":2,"w":3,"h":4},"line":0,"gloss":{"lemma":"λόγος","translation":"word","definition":"def"}},
                      {"text":"ἐστίν","probabilities":{"ἐστίν":0.8},"box":{"x":5,"y":6,"w":7,"h":8},"line":0,"gloss":{"lemma":"εἰμί","translation":"is","definition":"def"}}
                    ]
                  }
                ]
                """.trimIndent(),
        )
        val repository = HistoryRepositoryImpl(api)

        val items = repository.getHistory()

        assertEquals(1, items.size)
        assertEquals("page.png", items.first().filename)
        assertEquals("λόγος ἐστίν", items.first().previewText)
    }

    @Test
    fun getRecognitionDetailMapsPredictionAndImage() = runTest {
        val image = byteArrayOf(1, 2, 3, 4)
        val api = historyApi(
            detailBody =
                """
                {
                  "id": 7,
                  "created_at": "2026-11-03T10:30:00Z",
                  "filename": "saved.png",
                  "model_version": "eparchos",
                  "predictions": [
                    {"text":"λόγος","probabilities":{"λόγος":0.95},"box":{"x":10,"y":20,"w":30,"h":40},"line":0,"gloss":{"lemma":"λόγος","translation":"word","definition":"def"}}
                  ]
                }
                """.trimIndent(),
            imageBody = image,
        )
        val repository = HistoryRepositoryImpl(api)

        val detail = repository.getRecognitionDetail(7)

        assertEquals(7, detail.id)
        assertEquals("saved.png", detail.filename)
        assertEquals("eparchos", detail.modelVersion)
        assertEquals("λόγος", detail.prediction.predictions.single().text)
        assertContentEquals(image, detail.imageBytes)
    }

    @Test
    fun getRecognitionDetailPropagatesNotFound() = runTest {
        val api = historyApi(detailStatus = HttpStatusCode.NotFound)
        val repository = HistoryRepositoryImpl(api)

        val error = assertFailsWith<AuthApiException> { repository.getRecognitionDetail(55) }

        assertEquals(404, error.status)
    }

    private fun historyApi(
        listBody: String = "[]",
        detailBody: String =
            """
            {"id":1,"created_at":"2026-11-03T10:30:00Z","filename":"f.png","model_version":"m","predictions":[]}
            """.trimIndent(),
        detailStatus: HttpStatusCode = HttpStatusCode.OK,
        imageBody: ByteArray = byteArrayOf(),
    ): HistoryApi {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/recognitions" -> respond(
                    content = listBody,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
                "/recognitions/7", "/recognitions/55", "/recognitions/1" -> respond(
                    content = detailBody,
                    status = detailStatus,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
                "/recognitions/7/image", "/recognitions/55/image", "/recognitions/1/image" -> respond(
                    content = imageBody,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "image/png"),
                )
                else -> error("Unexpected path: ${request.url.encodedPath}")
            }
        }
        return HistoryApi(HttpClient(engine), "http://localhost:8000")
    }
}

