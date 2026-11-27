package org.example.project.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal val apiJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class ErrorResponseDto(val detail: String? = null)

internal fun apiException(status: Int, body: String): ApiException {
    val detail = runCatching { apiJson.decodeFromString<ErrorResponseDto>(body).detail }.getOrNull()
    return ApiException(status = status, detail = detail)
}
