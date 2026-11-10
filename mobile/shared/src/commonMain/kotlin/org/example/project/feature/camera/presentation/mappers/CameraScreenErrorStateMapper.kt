package org.example.project.feature.camera.presentation.mappers

import org.example.project.feature.camera.domain.model.ApiException
import org.example.project.feature.camera.presentation.models.CameraScreenErrorState

fun mapUploadErrorToUiModel(error: Throwable): CameraScreenErrorState {
    if (error !is ApiException) return CameraScreenErrorState.RecognitionFailed
    return when (error.status) {
        0, 502, 503, 504 -> CameraScreenErrorState.ServiceOffline
        413 -> CameraScreenErrorState.ImageTooLarge
        400, 415 -> CameraScreenErrorState.ImageRejected
        422 -> CameraScreenErrorState.ValidationFailed
        else -> CameraScreenErrorState.RecognitionFailed
    }
}
