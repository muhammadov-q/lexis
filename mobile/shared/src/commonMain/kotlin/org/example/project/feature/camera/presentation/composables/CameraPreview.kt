package org.example.project.feature.camera.presentation.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.ismoy.imagepickerkmp.picker.ImagePickerResult
import io.github.ismoy.imagepickerkmp.picker.rememberImagePickerKMP
import kotlinx.coroutines.flow.Flow
import org.example.project.feature.camera.presentation.CameraEffect
import org.example.project.feature.camera.presentation.helpers.loadPhotoBytes
import org.jetbrains.compose.resources.stringResource
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.error_camera_open
import lexis.shared.generated.resources.error_gallery_open
import lexis.shared.generated.resources.error_photo_select

@Composable
fun CameraPreview(
    cameraEffects: Flow<CameraEffect>,
    onPhotoCaptured: (ByteArray?) -> Unit,
    onCaptureError: (String) -> Unit,
    onImagePickerProcessingStarted: () -> Unit,
    onCaptureCancelled: () -> Unit
) {
    val cameraOpenError = stringResource(Res.string.error_camera_open)
    val galleryOpenError = stringResource(Res.string.error_gallery_open)
    val selectError = stringResource(Res.string.error_photo_select)
    val picker = rememberImagePickerKMP()
    val result = picker.result

    LaunchedEffect(cameraEffects, picker) {
        cameraEffects.collect { effect ->
            when (effect) {
                CameraEffect.CapturePhoto -> picker.launchCamera(
                    onError = { onCaptureError(it.message ?: cameraOpenError) },
                    onDismiss = onCaptureCancelled,
                )

                CameraEffect.SelectPhoto -> picker.launchGallery(
                    onError = { onCaptureError(it.message ?: galleryOpenError) },
                    onDismiss = onCaptureCancelled,
                )
            }
        }
    }

    LaunchedEffect(result) {
        when (result) {

            is ImagePickerResult.Success -> {
                onImagePickerProcessingStarted()
                val photoBytes = loadPhotoBytes(result.first)
                picker.reset()
                onPhotoCaptured(photoBytes)
            }

            is ImagePickerResult.Error -> {
                picker.reset()
                onCaptureError(result.exception.message ?: selectError)
            }

            is ImagePickerResult.Loading -> onImagePickerProcessingStarted()
            else -> Unit
        }
    }
}

