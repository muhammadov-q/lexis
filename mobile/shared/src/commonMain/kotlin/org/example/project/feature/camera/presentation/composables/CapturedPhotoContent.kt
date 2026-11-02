package org.example.project.feature.camera.presentation.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.composables.buttons.ScriptLensButton
import org.example.project.common.presentation.ui.composables.buttons.ScriptLensButtonStyle
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing
import org.example.project.feature.camera.presentation.composables.predictions.PredictionSummary
import org.example.project.feature.camera.presentation.helpers.decodePhoto
import org.example.project.feature.camera.presentation.models.PredictionBoxModel
import org.example.project.feature.camera.presentation.models.PredictionGlossModel
import org.example.project.feature.camera.presentation.models.PredictionModel
import org.example.project.feature.camera.presentation.models.WordPredictionModel
import org.jetbrains.compose.resources.stringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.action_choose_another_photo
import scriptlens.shared.generated.resources.action_take_new_photo
import scriptlens.shared.generated.resources.captured_photo_description

@Composable
fun CapturedPhotoContent(
    photo: ByteArray?,
    prediction: PredictionModel?,
    selectedPredictionIndex: Int?,
    onPredictionSelected: (Int) -> Unit,
    onTakeNewPhotoClicked: () -> Unit,
    onChooseFromPhotosClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageBitMap = photo?.let { remember(photo) { decodePhoto(photo) } }
    var zoomScale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val nextScale = (zoomScale * zoomChange).coerceIn(1f, 4f)
        zoomScale = nextScale
        panOffset = if (nextScale <= 1f) Offset.Zero else panOffset + panChange
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        imageBitMap?.let { image ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ScriptLensSpacing.padding_16)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .heightIn(min = 260.dp)
                    .aspectRatio(image.width.toFloat() / image.height.toFloat()),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .transformable(state = transformableState, canPan = { zoomScale > 1f })
                ) {
                    Image(
                        bitmap = image,
                        contentDescription = stringResource(Res.string.captured_photo_description),
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            },
                    )

                    PredictionOverlay(
                        image = image,
                        prediction = prediction,
                        selectedPredictionIndex = selectedPredictionIndex,
                        onPredictionSelected = onPredictionSelected,
                        zoomScale = zoomScale,
                        panOffset = panOffset,
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
        }

        PredictionSummary(
            modifier = Modifier.padding(top = ScriptLensSpacing.padding_16),
            prediction = prediction,
            selectedPredictionIndex = selectedPredictionIndex,
            onPredictionSelected = onPredictionSelected,
        )

        ScriptLensButton(
            modifier = Modifier.padding(vertical = ScriptLensSpacing.padding_8),
            text = stringResource(Res.string.action_take_new_photo),
            onClick = onTakeNewPhotoClicked,
            buttonStyle = ScriptLensButtonStyle.Primary
        )

        ScriptLensButton(
            modifier = Modifier.padding(bottom = ScriptLensSpacing.padding_4),
            text = stringResource(Res.string.action_choose_another_photo),
            onClick = onChooseFromPhotosClicked,
            buttonStyle = ScriptLensButtonStyle.Secondary
        )

    }
}

@Preview(showBackground = true)
@Composable
private fun CapturedPhotoPreview() {
    MaterialTheme {
        CapturedPhotoContent(
            photo = null,
            prediction = PredictionModel(
                predictions = listOf(
                    WordPredictionModel(
                        text = "logos",
                        probabilities = mapOf("logos" to 0.92),
                        box = PredictionBoxModel(80.0, 60.0, 120.0, 180.0),
                        line = 0,
                        gloss = PredictionGlossModel(
                            lemma = "logos",
                            translation = "word",
                            definition = "A word or account.",
                        ),
                    ),
                    WordPredictionModel(
                        text = "esti",
                        probabilities = mapOf("esti" to 0.87),
                        box = PredictionBoxModel(240.0, 60.0, 120.0, 180.0),
                        line = 0,
                        gloss = PredictionGlossModel(
                            lemma = "eimi",
                            translation = "is",
                            definition = "Third-person singular of to be.",
                        ),
                    ),
                ),
                modelVersion = "preview",
            ),
            selectedPredictionIndex = 0,
            onPredictionSelected = {},
            onTakeNewPhotoClicked = {},
            onChooseFromPhotosClicked = {},
        )
    }
}
