package org.example.project.feature.auth.domain.model

data class User(
    val id: Int,
    val email: String,
    val firstName: String?,
    val lastName: String?,
)

