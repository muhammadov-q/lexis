package org.example.project.feature.camera.presentation.composables

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.example.project.feature.camera.presentation.models.PredictionBoxModel
import org.example.project.feature.camera.presentation.models.PredictionGlossModel
import org.example.project.feature.camera.presentation.models.WordPredictionModel
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PredictionOverlayGeometryTest {
    @Test
    fun selectPredictionIndexAtOneXZoom() {
        val predictions = listOf(
            prediction(x = 100.0, y = 50.0, w = 120.0, h = 80.0),
            prediction(x = 260.0, y = 50.0, w = 120.0, h = 80.0),
        )

        val overlayTap = imagePointToOverlayPoint(
            imageX = 150.0,
            imageY = 80.0,
            imageSize = IMAGE_SIZE,
            containerSize = CONTAINER_SIZE,
            zoomScale = 1f,
            panOffset = Offset.Zero,
        )

        val selected = selectPredictionIndexForTap(
            tap = overlayTap!!,
            predictions = predictions,
            imageSize = IMAGE_SIZE,
            containerSize = CONTAINER_SIZE,
            zoomScale = 1f,
            panOffset = Offset.Zero,
        )

        assertEquals(0, selected)
    }

    @Test
    fun selectPredictionIndexAtMaxZoomWithPan() {
        val predictions = listOf(
            prediction(x = 100.0, y = 100.0, w = 100.0, h = 100.0),
            prediction(x = 300.0, y = 120.0, w = 80.0, h = 80.0),
        )
        val zoom = 4f
        val pan = Offset(30f, -24f)

        val overlayTap = imagePointToOverlayPoint(
            imageX = 340.0,
            imageY = 140.0,
            imageSize = IMAGE_SIZE,
            containerSize = CONTAINER_SIZE,
            zoomScale = zoom,
            panOffset = pan,
        )

        val selected = selectPredictionIndexForTap(
            tap = overlayTap!!,
            predictions = predictions,
            imageSize = IMAGE_SIZE,
            containerSize = CONTAINER_SIZE,
            zoomScale = zoom,
            panOffset = pan,
        )

        assertEquals(1, selected)
    }

    @Test
    fun selectPredictionIgnoresInvalidBoxes() {
        val predictions = listOf(
            prediction(x = -10.0, y = 20.0, w = 0.0, h = 10.0),
            prediction(x = Double.NaN, y = 20.0, w = 10.0, h = 10.0),
            prediction(x = 100.0, y = 100.0, w = 40.0, h = 30.0),
        )
        val tap = imagePointToOverlayPoint(
            imageX = 110.0,
            imageY = 110.0,
            imageSize = IMAGE_SIZE,
            containerSize = CONTAINER_SIZE,
            zoomScale = 1f,
            panOffset = Offset.Zero,
        )

        val selected = selectPredictionIndexForTap(
            tap = tap!!,
            predictions = predictions,
            imageSize = IMAGE_SIZE,
            containerSize = CONTAINER_SIZE,
            zoomScale = 1f,
            panOffset = Offset.Zero,
        )

        assertEquals(2, selected)
    }

    @Test
    fun overlayRectClipsToImageBounds() {
        val rect = overlayRectForImageBox(
            x = -50.0,
            y = -25.0,
            w = 120.0,
            h = 80.0,
            imageSize = IMAGE_SIZE,
            containerSize = CONTAINER_SIZE,
            zoomScale = 1f,
            panOffset = Offset.Zero,
        )

        assertNotNull(rect)
        assertClose(0f, rect.left)
        assertClose(0f, rect.top)
        assertClose(35f, rect.right)
        assertClose(27.5f, rect.bottom)
    }

    @Test
    fun overlayRectReturnsNullForBoxesOutsideImage() {
        val rect = overlayRectForImageBox(
            x = 900.0,
            y = 700.0,
            w = 100.0,
            h = 100.0,
            imageSize = IMAGE_SIZE,
            containerSize = CONTAINER_SIZE,
            zoomScale = 1f,
            panOffset = Offset.Zero,
        )

        assertNull(rect)
    }

    private fun prediction(x: Double, y: Double, w: Double, h: Double) =
        WordPredictionModel(
            text = "word",
            probabilities = emptyMap(),
            box = PredictionBoxModel(x, y, w, h),
            line = 0,
            gloss = PredictionGlossModel("", "", ""),
        )

    private fun assertClose(expected: Float, actual: Float) {
        val diff = abs(expected - actual)
        if (diff > 0.001f) {
            throw AssertionError("Expected <$expected> but was <$actual> (diff=$diff)")
        }
    }

    private companion object {
        val IMAGE_SIZE = IntSize(800, 600)
        val CONTAINER_SIZE = IntSize(400, 300)
    }
}

