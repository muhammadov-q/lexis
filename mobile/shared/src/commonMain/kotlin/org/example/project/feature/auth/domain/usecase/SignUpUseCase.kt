package org.example.project.feature.auth.domain.usecase

import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.repository.AuthRepository

class SignUpUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): User = repository.signUp(
        email = email,
        password = password,
        firstName = firstName,
        lastName = lastName,
    )
}

