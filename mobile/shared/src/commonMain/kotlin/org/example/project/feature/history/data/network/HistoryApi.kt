package org.example.project.feature.history.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import org.example.project.core.network.ApiConfig
import org.example.project.core.network.ApiException
import org.example.project.core.network.apiException
import org.example.project.core.network.apiJson
import org.example.project.feature.history.data.network.models.RecognitionDto

class HistoryApi(
    private val client: HttpClient,
    private val config: ApiConfig,
) {

    suspend fun getRecognitions(): List<RecognitionDto> {
        val endpoint = config.url("recognitions")
        return responseBodyOrThrow(clientGet(endpoint))
    }

    suspend fun getRecognition(id: Long): RecognitionDto {
        val endpoint = config.url("recognitions/$id")
        return responseBodyOrThrow(clientGet(endpoint))
    }

    suspend fun getRecognitionImage(id: Long): ByteArray {
        val endpoint = config.url("recognitions/$id/image")
        val response = clientGet(endpoint)
        if (response.status.value != HttpStatusCode.OK.value) {
            throw apiException(response.status.value, response.bodyAsText())
        }
        return response.body()
    }

    private suspend fun clientGet(endpoint: String): HttpResponse {
        return try {
            client.get(endpoint)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            throw ApiException(ApiException.NO_RESPONSE)
        }
    }

    private suspend inline fun <reified T> responseBodyOrThrow(response: HttpResponse): T {
        val status = response.status.value
        val body = response.bodyAsText()
        if (status != HttpStatusCode.OK.value) {
            throw apiException(status, body)
        }
        return apiJson.decodeFromString(body)
    }
}

