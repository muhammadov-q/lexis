package org.example.project.feature.camera.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.common.presentation.ui.composables.loading.OverlayLoadingState
import org.example.project.common.presentation.ui.theme.ScriptLensColors
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing
import org.example.project.common.presentation.ui.theme.ScriptLensTypography
import org.example.project.feature.camera.presentation.composables.CameraPreview
import org.example.project.feature.camera.presentation.composables.CapturedPhotoContent
import org.example.project.feature.camera.presentation.composables.EmptyCameraState
import org.example.project.feature.camera.presentation.composables.error.ErrorState
import org.example.project.feature.camera.presentation.models.CameraScreenState
import org.jetbrains.compose.resources.stringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.camera_subtitle
import scriptlens.shared.generated.resources.camera_title

@Composable
fun CameraScreen(viewModel: CameraViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CameraScreenContent(
        state = state,
        onTakePhotoClicked = { viewModel.onEvent(CameraEvent.CapturePhotoRequested) },
        onChooseFromPhotosClicked = { viewModel.onEvent(CameraEvent.SelectPhotoRequested) },
        onRetryClicked = { viewModel.onEvent(CameraEvent.RetryUploadRequested) },
        onPredictionSelected = { viewModel.onEvent(CameraEvent.SelectPrediction(it)) },
    )

    CameraPreview(
        cameraEffects = viewModel.effects,
        onPhotoCaptured = { photo -> viewModel.onEvent(CameraEvent.OnPhotoPicked(photo)) },
        onCaptureCancelled = { viewModel.onEvent(CameraEvent.OnPhotoCaptureCancelled) },
        onCaptureError = { message ->
            viewModel.onEvent(CameraEvent.OnPhotoCaptureFailed(message))
        },
        onImagePickerProcessingStarted = {
            viewModel.onEvent(CameraEvent.OnImagePickerProcessingStarted)
        }
    )

    AnimatedVisibility(
        visible = state.isLoading,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
    ) {
        OverlayLoadingState()
    }

}

@Composable
private fun CameraScreenContent(
    state: CameraContract,
    onTakePhotoClicked: () -> Unit,
    onChooseFromPhotosClicked: () -> Unit,
    onRetryClicked: () -> Unit,
    onPredictionSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ScriptLensSpacing.padding_24)
    ) {

        Text(
            text = stringResource(Res.string.camera_title),
            style = ScriptLensTypography.Title
        )

        Text(
            text = stringResource(Res.string.camera_subtitle),
            style = ScriptLensTypography.Body,
            color = ScriptLensColors.Subtext
        )


        AnimatedContent(
            targetState = state.cameraScreenState,
            contentKey = { it::class },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
            label = "cameraScreenState",
        ) { cameraScreenState ->
            Box(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                contentAlignment = if (cameraScreenState is CameraScreenState.PhotoCaptured) {
                    Alignment.TopCenter
                } else {
                    Alignment.Center
                },
            ) {
                when (cameraScreenState) {
                    is CameraScreenState.NoPhotoCaptured ->
                        EmptyCameraState(
                            onTakePhotoClicked = onTakePhotoClicked,
                            onChooseFromPhotosClicked = onChooseFromPhotosClicked
                        )

                    is CameraScreenState.PhotoCaptured ->
                        CapturedPhotoContent(
                            modifier = Modifier.padding(top = ScriptLensSpacing.padding_16),
                            photo = state.photo,
                            prediction = state.prediction,
                            selectedPredictionIndex = state.selectedPredictionIndex,
                            onPredictionSelected = onPredictionSelected,
                            onTakeNewPhotoClicked = onTakePhotoClicked,
                            onChooseFromPhotosClicked = onChooseFromPhotosClicked
                        )

                    is CameraScreenState.Error ->
                        ErrorState(
                            errorState = cameraScreenState.error,
                            onTakeNewPhotoClicked = onTakePhotoClicked,
                            onChooseFromPhotosClicked = onChooseFromPhotosClicked,
                            onRetryClicked = onRetryClicked
                        )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CameraScreenPreview(
    @PreviewParameter(CameraContractPreviewProvider::class) state: CameraContract,
) {
    MaterialTheme {
        CameraScreenContent(
            state = state,
            onTakePhotoClicked = {},
            onChooseFromPhotosClicked = {},
            onRetryClicked = {},
            onPredictionSelected = {},
        )
    }
}

