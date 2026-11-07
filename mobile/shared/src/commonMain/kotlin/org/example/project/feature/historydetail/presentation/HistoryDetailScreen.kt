package org.example.project.feature.historydetail.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.common.presentation.ui.helpers.asString
import org.example.project.common.presentation.ui.theme.ScriptLensColors
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing
import org.example.project.feature.camera.presentation.composables.PredictionOverlay
import org.example.project.feature.camera.presentation.composables.predictions.PredictionSummary
import org.example.project.feature.camera.presentation.helpers.decodePhoto
import org.example.project.feature.camera.presentation.mappers.mapPredictionToUiModel
import org.example.project.feature.history.presentation.composables.GuestHistoryPrompt
import org.example.project.feature.history.presentation.composables.HistoryErrorCard
import org.example.project.feature.history.presentation.composables.UnauthorizedHistoryCard
import org.example.project.feature.history.presentation.formatHistoryDateTime
import org.jetbrains.compose.resources.stringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.history_detail_image_unavailable
import scriptlens.shared.generated.resources.history_detail_model_version
import scriptlens.shared.generated.resources.history_detail_read_on

@Composable
fun HistoryDetailScreen(
    id: Long,
    isAuthenticated: Boolean,
    viewModel: HistoryDetailViewModel,
    onSignInRequested: () -> Unit,
    onCreateAccountRequested: () -> Unit,
) {
    if (!isAuthenticated) {
        GuestHistoryPrompt(
            onSignInRequested = onSignInRequested,
            onCreateAccountRequested = onCreateAccountRequested,
        )
        return
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, id) {
        viewModel.load(id)
    }

    Box(modifier = Modifier.fillMaxSize().background(ScriptLensColors.Background)) {
        HistoryDetailContent(
            state = state,
            onRetry = { viewModel.load(id) },
            onPredictionSelected = viewModel::selectPrediction,
            onSignInRequested = onSignInRequested,
        )
    }
}

@Composable
private fun HistoryDetailContent(
    state: HistoryDetailContract,
    onRetry: () -> Unit,
    onPredictionSelected: (Int) -> Unit,
    onSignInRequested: () -> Unit,
) {
    if (state.requiresSignIn) {
        UnauthorizedHistoryCard(onSignInRequested)
        return
    }

    val detail = state.selectedDetail
    if (state.isLoading && detail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.error != null && detail == null) {
        HistoryErrorCard(message = state.error.asString(), onRetry = onRetry)
        return
    }

    detail ?: return
    val predictionModel = remember(detail.id) { mapPredictionToUiModel(detail.prediction) }
    val imageBitmap = remember(detail.id) { decodePhoto(detail.imageBytes) }

    var zoomScale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val nextScale = (zoomScale * zoomChange).coerceIn(1f, 4f)
        zoomScale = nextScale
        panOffset = if (nextScale <= 1f) Offset.Zero else panOffset + panChange
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ScriptLensSpacing.padding_24),
        verticalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_12),
    ) {
        Text(
            text = if (detail.filename.isBlank()) "Untitled page" else detail.filename,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(Res.string.history_detail_read_on, formatHistoryDateTime(detail.createdAt)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.history_detail_model_version, detail.modelVersion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (imageBitmap == null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = ScriptLensSpacing.padding_56 * 4)
                    .clip(RoundedCornerShape(ScriptLensSpacing.padding_12)),
                color = ScriptLensColors.Muted,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.history_detail_image_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .heightIn(min = ScriptLensSpacing.padding_56 * 4)
                    .aspectRatio(imageBitmap.width.toFloat() / imageBitmap.height.toFloat()),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .transformable(state = transformableState, canPan = { zoomScale > 1f }),
                ) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = detail.filename,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            },
                    )
                    PredictionOverlay(
                        image = imageBitmap,
                        prediction = predictionModel,
                        selectedPredictionIndex = state.selectedPredictionIndex,
                        onPredictionSelected = onPredictionSelected,
                        zoomScale = zoomScale,
                        panOffset = panOffset,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        PredictionSummary(
            prediction = predictionModel,
            selectedPredictionIndex = state.selectedPredictionIndex,
            onPredictionSelected = onPredictionSelected,
        )
    }
}
