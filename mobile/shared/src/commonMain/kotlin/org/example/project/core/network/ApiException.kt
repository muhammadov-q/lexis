package org.example.project.core.network

class ApiException(
    val status: Int,
    val detail: String? = null,
) : Exception(detail ?: "Request failed with status $status") {
    companion object {
        const val NO_RESPONSE = 0
    }
}
