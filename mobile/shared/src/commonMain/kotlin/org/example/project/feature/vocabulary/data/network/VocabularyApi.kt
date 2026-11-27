package org.example.project.feature.vocabulary.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import org.example.project.core.network.ApiConfig
import org.example.project.core.network.ApiException
import org.example.project.core.network.apiException
import org.example.project.core.network.apiJson
import org.example.project.feature.vocabulary.data.network.models.AddWordRequestDto
import org.example.project.feature.vocabulary.data.network.models.NoteRequestDto
import org.example.project.feature.vocabulary.data.network.models.VocabularyWordDto

class VocabularyApi(
    private val client: HttpClient,
    private val config: ApiConfig,
) {

    suspend fun list(): List<VocabularyWordDto> =
        decode(request { client.get(endpoint()) }, HttpStatusCode.OK)

    suspend fun get(id: Long): VocabularyWordDto =
        decode(request { client.get(endpoint(id)) }, HttpStatusCode.OK)

    suspend fun add(recognitionId: Long, wordIndex: Int, note: String): VocabularyWordDto {
        val body = apiJson.encodeToString(AddWordRequestDto(recognitionId, wordIndex, note))
        val response = request {
            client.post(endpoint()) {
                header(HttpHeaders.ContentType, "application/json")
                setBody(body)
            }
        }
        return decode(response, HttpStatusCode.Created)
    }

    suspend fun updateNote(id: Long, note: String): VocabularyWordDto {
        val body = apiJson.encodeToString(NoteRequestDto(note))
        val response = request {
            client.patch(endpoint(id)) {
                header(HttpHeaders.ContentType, "application/json")
                setBody(body)
            }
        }
        return decode(response, HttpStatusCode.OK)
    }

    suspend fun remove(id: Long) {
        val response = request { client.delete(endpoint(id)) }
        if (response.status != HttpStatusCode.NoContent.value) throw apiException(response.status, response.body)
    }

    private fun endpoint(id: Long? = null): String {
        val base = config.url("vocabulary")
        return if (id == null) base else "$base/$id"
    }

    private inline fun <reified T> decode(response: RawResponse, expected: HttpStatusCode): T {
        if (response.status != expected.value) throw apiException(response.status, response.body)
        return apiJson.decodeFromString(response.body)
    }

    private suspend fun request(call: suspend () -> HttpResponse): RawResponse {
        return try {
            val response = call()
            RawResponse(status = response.status.value, body = response.bodyAsText())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            throw ApiException(ApiException.NO_RESPONSE)
        }
    }

    private class RawResponse(val status: Int, val body: String)
}
