package org.example.project.feature.camera.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import org.example.project.feature.camera.domain.model.ApiException
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import kotlinx.serialization.json.Json
import org.example.project.feature.camera.data.network.models.PredictionDto
import org.example.project.feature.camera.data.network.models.OcrModelsDto

class PhotoUploadApi(
    private val client: HttpClient,
    private val config: PhotoUploadConfig,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getModels(): OcrModelsDto {
        val response = try {
            client.get("${config.baseUrl.trimEnd('/')}/models")
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            println("$TAG: request failed endpoint=models error=${exception::class.simpleName}")
            throw ApiException(0)
        }
        if (response.status != HttpStatusCode.OK) throw ApiException(response.status.value)
        return json.decodeFromString(response.bodyAsText())
    }

    suspend fun upload(photo: ByteArray, uploadPath: String, model: String? = null): PredictionDto {
        val endpoint = "${config.baseUrl.trimEnd('/')}/${uploadPath.trimStart('/')}"
        val endpointId = uploadPath.trimStart('/').ifBlank { "predict" }

        val status: Int
        val body: String
        try {
            val response = client.post(endpoint) { setBody(photoBody(photo, model)) }
            status = response.status.value
            body = response.bodyAsText()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            val errorSummary = exception::class.simpleName ?: "Exception"
            println("$TAG: request failed endpoint=$endpointId status=0 error=$errorSummary")
            throw ApiException(0)
        }

        println("$TAG: response endpoint=$endpointId status=$status")
        if (status != HttpStatusCode.OK.value && status != HttpStatusCode.Created.value) {
            throw ApiException(status)
        }
        return json.decodeFromString(body)
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
        const val TAG = "PhotoUploadApi"
    }
}
