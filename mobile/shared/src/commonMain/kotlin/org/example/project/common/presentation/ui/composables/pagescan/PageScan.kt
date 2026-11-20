package org.example.project.common.presentation.ui.composables.pagescan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.helpers.lineCenters
import org.example.project.common.presentation.models.PredictionModel
import org.example.project.common.presentation.ui.preview.previewPage
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography

private val LineNumbersWidth = 28.dp

@Composable
fun PageScan(
    image: ImageBitmap,
    contentDescription: String,
    prediction: PredictionModel?,
    selectedPredictionIndex: Int?,
    onPredictionSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var zoomScale by remember(image) { mutableStateOf(1f) }
    var panOffset by remember(image) { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(1f, 4f)
        panOffset = if (zoomScale <= 1f) Offset.Zero else panOffset + panChange
    }

    val words = prediction?.predictions.orEmpty()
    val centers = remember(words) { lineCenters(words) }
    val selectedLine = selectedPredictionIndex?.let { words.getOrNull(it)?.line }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(LexisColors.Surface)
            .padding(LexisSpacing.padding_12),
    ) {
        val numbersWidth = if (centers.isEmpty()) 0.dp else LineNumbersWidth
        val imageHeight = (maxWidth - numbersWidth) * image.height.toFloat() / image.width.toFloat()

        Row(
            modifier = Modifier.fillMaxWidth().height(imageHeight),
            horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4),
        ) {
            if (centers.isNotEmpty()) {
                Box(modifier = Modifier.width(LineNumbersWidth - LexisSpacing.padding_4).fillMaxHeight()) {
                    if (zoomScale == 1f) {
                        centers.forEach { (line, center) ->
                            LineNumber(
                                number = line + 1,
                                isSelected = line == selectedLine,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(y = imageHeight * (center / image.height).toFloat() - 8.dp),
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .transformable(state = transformableState, canPan = { zoomScale > 1f }),
            ) {
                Image(
                    bitmap = image,
                    contentDescription = contentDescription,
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
                    image = image,
                    prediction = prediction,
                    selectedPredictionIndex = selectedPredictionIndex,
                    onPredictionSelected = onPredictionSelected,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun LineNumber(number: Int, isSelected: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = "$number",
        style = LexisTypography.Annotation,
        color = if (isSelected) LexisColors.OnAccent else LexisColors.Subtext,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) LexisColors.Accent else LexisColors.Surface)
            .padding(horizontal = 3.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun PageScanPreview(
    @PreviewParameter(PageScanPreviewProvider::class) state: PageScanPreviewState,
) {
    MaterialTheme {
        PageScan(
            image = previewPage(),
            contentDescription = "Page",
            prediction = state.prediction,
            selectedPredictionIndex = state.selectedPredictionIndex,
            onPredictionSelected = {},
        )
    }
}
