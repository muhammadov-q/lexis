package org.example.project.feature.camera.domain.repository

import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.OcrModels

interface PhotoRepository {
    suspend fun uploadPhoto(photo: ByteArray, model: String? = null): Prediction
    suspend fun getModels(): OcrModels
}
