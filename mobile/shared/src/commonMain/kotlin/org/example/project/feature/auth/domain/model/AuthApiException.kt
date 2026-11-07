package org.example.project.feature.auth.domain.model

class AuthApiException(
    val status: Int,
    val detail: String? = null,
) : Exception(detail ?: "Auth request failed with status $status")

