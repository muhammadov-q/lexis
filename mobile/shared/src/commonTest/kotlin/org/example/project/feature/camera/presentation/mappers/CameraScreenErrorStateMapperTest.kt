package org.example.project.feature.camera.presentation.mappers

import org.example.project.feature.camera.domain.model.ApiException
import org.example.project.feature.camera.presentation.models.CameraScreenErrorState
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals

class CameraScreenErrorStateMapperTest {
    @Test
    fun noResponseMapsToServiceOffline() {
        assertEquals(CameraScreenErrorState.ServiceOffline, mapUploadErrorToUiModel(ApiException(0)))
    }

    @Test
    fun gatewayAndServiceUnavailableStatusesMapToServiceOffline() {
        listOf(502, 503, 504).forEach { status ->
            assertEquals(
                CameraScreenErrorState.ServiceOffline,
                mapUploadErrorToUiModel(ApiException(status)),
                "status $status",
            )
        }
    }

    @Test
    fun tooLargeMapsToImageTooLarge() {
        assertEquals(CameraScreenErrorState.ImageTooLarge, mapUploadErrorToUiModel(ApiException(413)))
    }

    @Test
    fun rejectedStatusesMapToImageRejected() {
        listOf(400, 415).forEach { status ->
            assertEquals(CameraScreenErrorState.ImageRejected, mapUploadErrorToUiModel(ApiException(status)), "status $status")
        }

    }

    @Test
    fun validationFailureCanIndicateAnInvalidModel() {
        assertEquals(CameraScreenErrorState.ValidationFailed, mapUploadErrorToUiModel(ApiException(422)))
    }

    @Test
    fun otherStatusesMapToRecognitionFailed() {
        listOf(401, 404, 500, 501, 505).forEach { status ->
            assertEquals(CameraScreenErrorState.RecognitionFailed, mapUploadErrorToUiModel(ApiException(status)), "status $status")
        }
    }

    @Test
    fun nonApiExceptionMapsToRecognitionFailed() {
        assertEquals(CameraScreenErrorState.RecognitionFailed, mapUploadErrorToUiModel(IllegalStateException("boom")))
    }

    @Test
    fun serializationErrorMapsToRecognitionFailed() {
        assertEquals(CameraScreenErrorState.RecognitionFailed, mapUploadErrorToUiModel(SerializationException("bad body")))
    }
}
