package org.example.project.feature.camera.presentation

import org.example.project.feature.camera.presentation.models.CameraScreenState
import org.example.project.feature.camera.presentation.models.PredictionModel
import org.example.project.feature.camera.domain.model.OcrModel

data class CameraContract(
    val cameraScreenState: CameraScreenState = CameraScreenState.NoPhotoCaptured,
    val photo: ByteArray? = null,
    val prediction: PredictionModel? = null,
    val selectedPredictionIndex: Int? = null,
    val isLoading: Boolean = false,
    val models: List<OcrModel> = emptyList(),
    val selectedModel: String? = null,
    val modelsLoading: Boolean = true,
    val modelsError: Boolean = false,
)

sealed interface CameraEvent {
    data object CapturePhotoRequested : CameraEvent
    data object SelectPhotoRequested : CameraEvent
    class OnPhotoPicked(val photoBytes: ByteArray?) : CameraEvent
    data object OnPhotoCaptureCancelled : CameraEvent
    data class OnPhotoCaptureFailed(val message: String) : CameraEvent
    data object RetryUploadRequested : CameraEvent
    data class SelectPrediction(val index: Int) : CameraEvent
    data class SelectModel(val id: String) : CameraEvent
    data object ReloadModels : CameraEvent
    data object ReadAgain : CameraEvent

    data object OnImagePickerProcessingStarted : CameraEvent

}

sealed interface CameraEffect {
    data object CapturePhoto : CameraEffect
    data object SelectPhoto : CameraEffect
}
