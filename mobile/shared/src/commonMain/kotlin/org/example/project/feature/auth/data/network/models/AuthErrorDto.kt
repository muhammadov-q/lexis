package org.example.project.feature.auth.data.network.models

import kotlinx.serialization.Serializable

@Serializable
internal data class AuthErrorDto(
    val detail: String? = null,
)

