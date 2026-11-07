package org.example.project.feature.auth.domain.repository

import org.example.project.feature.auth.domain.model.User

interface AuthRepository {
    suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): User

    suspend fun signIn(email: String, password: String): User

    suspend fun signOut()

    suspend fun currentUserOrNull(): User?
}

