package org.example.project.feature.auth.domain.usecase

import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.repository.AuthRepository

class GetCurrentUserUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): User? = repository.currentUserOrNull()
}

