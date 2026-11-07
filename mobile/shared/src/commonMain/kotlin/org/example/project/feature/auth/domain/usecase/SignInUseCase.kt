package org.example.project.feature.auth.domain.usecase

import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.repository.AuthRepository

class SignInUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): User =
        repository.signIn(email = email, password = password)
}

