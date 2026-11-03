package org.example.project.feature.camera.presentation.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.ScriptLensColors
import org.example.project.feature.camera.presentation.models.PredictionModel

@Composable
fun PredictionOverlay(
    image: ImageBitmap,
    prediction: PredictionModel?,
    selectedPredictionIndex: Int?,
    onPredictionSelected: (Int) -> Unit,
    zoomScale: Float,
    panOffset: Offset,
    modifier: Modifier = Modifier,
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val predictions = prediction?.predictions.orEmpty()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(predictions, zoomScale, panOffset, containerSize) {
                detectTapGestures { tap ->
                    if (containerSize.width == 0 || containerSize.height == 0 || predictions.isEmpty()) return@detectTapGestures
                    selectPredictionIndexForTap(
                        tap = tap,
                        predictions = predictions,
                        imageSize = IntSize(image.width, image.height),
                        containerSize = containerSize,
                        zoomScale = zoomScale,
                        panOffset = panOffset,
                    )?.let(onPredictionSelected)
                }
            },
    ) {
        val selectedWord = selectedPredictionIndex
            ?.takeIf { it in predictions.indices }
            ?.let { predictions[it] }

        selectedWord?.let { word ->
            val rect = overlayRectForImageBox(
                x = word.box.x,
                y = word.box.y,
                w = word.box.w,
                h = word.box.h,
                imageSize = IntSize(image.width, image.height),
                containerSize = IntSize(size.width.toInt(), size.height.toInt()),
                zoomScale = zoomScale,
                panOffset = panOffset,
            ) ?: return@let

            drawRect(
                color = ScriptLensColors.Accent,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}
