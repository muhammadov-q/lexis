package org.example.project.feature.camera.presentation.mappers

import org.example.project.core.network.ApiException
import org.example.project.feature.camera.presentation.models.CameraScreenErrorState

fun mapUploadErrorToUiModel(error: Throwable): CameraScreenErrorState {
    if (error !is ApiException) return CameraScreenErrorState.RecognitionFailed
    return when (error.status) {
        ApiException.NO_RESPONSE,
        ApiException.BAD_GATEWAY,
        ApiException.SERVICE_UNAVAILABLE,
        ApiException.GATEWAY_TIMEOUT -> CameraScreenErrorState.ServiceOffline
        ApiException.PAYLOAD_TOO_LARGE -> CameraScreenErrorState.ImageTooLarge
        ApiException.BAD_REQUEST, ApiException.UNSUPPORTED_MEDIA_TYPE -> CameraScreenErrorState.ImageRejected
        ApiException.UNPROCESSABLE_ENTITY -> CameraScreenErrorState.ValidationFailed
        else -> CameraScreenErrorState.RecognitionFailed
    }
}
