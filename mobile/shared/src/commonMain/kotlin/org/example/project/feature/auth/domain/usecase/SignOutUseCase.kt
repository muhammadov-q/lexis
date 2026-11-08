package org.example.project.feature.auth.domain.usecase

import org.example.project.feature.auth.domain.repository.AuthRepository

class SignOutUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() {
        repository.signOut()
    }
}

