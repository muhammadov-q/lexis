package org.example.project.core.network

class ApiException(
    val status: Int,
    val detail: String? = null,
) : Exception(detail ?: "Request failed with status $status") {
    val isUnauthorized: Boolean get() = status == UNAUTHORIZED

    companion object {
        const val NO_RESPONSE = 0
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val NOT_FOUND = 404
        const val CONFLICT = 409
        const val PAYLOAD_TOO_LARGE = 413
        const val UNSUPPORTED_MEDIA_TYPE = 415
        const val UNPROCESSABLE_ENTITY = 422
        const val BAD_GATEWAY = 502
        const val SERVICE_UNAVAILABLE = 503
        const val GATEWAY_TIMEOUT = 504
    }
}
