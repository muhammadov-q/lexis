package org.example.project.feature.auth.data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class SignInRequestDto(
    val email: String,
    val password: String,
)

