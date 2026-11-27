package org.example.project.feature.camera.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import org.example.project.core.network.ApiConfig
import org.example.project.core.network.ApiException
import org.example.project.core.network.apiException
import org.example.project.core.network.apiJson
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import org.example.project.feature.camera.data.network.models.PredictionDto
import org.example.project.feature.camera.data.network.models.OcrModelsDto

class PhotoUploadApi(
    private val client: HttpClient,
    private val apiConfig: ApiConfig,
    private val config: PhotoUploadConfig,
) {

    suspend fun getModels(): OcrModelsDto {
        val response = try {
            client.get(apiConfig.url("models"))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            throw ApiException(ApiException.NO_RESPONSE)
        }
        val body = response.bodyAsText()
        if (response.status != HttpStatusCode.OK) throw apiException(response.status.value, body)
        return apiJson.decodeFromString(body)
    }

    suspend fun upload(photo: ByteArray, uploadPath: String, model: String? = null): PredictionDto {
        val endpoint = apiConfig.url(uploadPath)

        val status: Int
        val body: String
        try {
            val response = client.post(endpoint) {
                timeout {
                    requestTimeoutMillis = UPLOAD_TIMEOUT_MILLIS
                    socketTimeoutMillis = UPLOAD_TIMEOUT_MILLIS
                }
                setBody(photoBody(photo, model))
            }
            status = response.status.value
            body = response.bodyAsText()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            throw ApiException(ApiException.NO_RESPONSE)
        }
        if (status != HttpStatusCode.OK.value && status != HttpStatusCode.Created.value) {
            throw apiException(status, body)
        }
        return apiJson.decodeFromString(body)
    }

    private fun photoBody(photo: ByteArray, model: String?) = MultiPartFormDataContent(
        formData {
            append(
                key = config.multipartFieldName,
                value = photo,
                headers = Headers.build {
                    append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                    append(HttpHeaders.ContentDisposition, "filename=\"photo.jpg\"")
                }
            )
            if (model != null) append("model", model)
        }
    )

    private companion object {
        const val UPLOAD_TIMEOUT_MILLIS = 360_000L // 6 minutes
    }
}
