package org.example.project.feature.camera.domain.usecase

import org.example.project.feature.camera.domain.repository.PhotoRepository
import org.example.project.feature.camera.domain.model.Prediction

class UploadPhotoUseCase(
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(photo: ByteArray, model: String? = null): Prediction =
        photoRepository.uploadPhoto(photo, model)
}
