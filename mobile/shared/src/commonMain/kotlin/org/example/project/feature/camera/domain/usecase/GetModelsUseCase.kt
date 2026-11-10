package org.example.project.feature.camera.domain.usecase

import org.example.project.feature.camera.domain.repository.PhotoRepository

class GetModelsUseCase(private val repository: PhotoRepository) {
    suspend operator fun invoke() = repository.getModels()
}
