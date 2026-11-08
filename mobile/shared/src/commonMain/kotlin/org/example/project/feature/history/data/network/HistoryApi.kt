package org.example.project.feature.history.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import org.example.project.feature.auth.domain.model.AuthApiException
import org.example.project.feature.history.data.network.models.RecognitionDto

class HistoryApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getRecognitions(): List<RecognitionDto> {
        val endpoint = "${baseUrl.trimEnd('/')}/recognitions"
        return responseBodyOrThrow(clientGet(endpoint))
    }

    suspend fun getRecognition(id: Long): RecognitionDto {
        val endpoint = "${baseUrl.trimEnd('/')}/recognitions/$id"
        return responseBodyOrThrow(clientGet(endpoint))
    }

    suspend fun getRecognitionImage(id: Long): ByteArray {
        val endpoint = "${baseUrl.trimEnd('/')}/recognitions/$id/image"
        val response = clientGet(endpoint)
        if (response.status.value != HttpStatusCode.OK.value) {
            throw AuthApiException(status = response.status.value)
        }
        return response.body()
    }

    private suspend fun clientGet(endpoint: String): HttpResponse {
        return try {
            client.get(endpoint)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            throw AuthApiException(status = 0)
        }
    }

    private suspend inline fun <reified T> responseBodyOrThrow(response: HttpResponse): T {
        val status = response.status.value
        val body = response.bodyAsText()
        if (status != HttpStatusCode.OK.value) {
            throw AuthApiException(status = status)
        }
        return json.decodeFromString(body)
    }
}

