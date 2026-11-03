package org.example.project.feature.camera.presentation.composables

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import org.example.project.feature.camera.presentation.models.WordPredictionModel
import kotlin.math.max
import kotlin.math.min

internal fun selectPredictionIndexForTap(
    tap: Offset,
    predictions: List<WordPredictionModel>,
    imageSize: IntSize,
    containerSize: IntSize,
    zoomScale: Float,
    panOffset: Offset,
): Int? {
    val imageTap = overlayTapToImagePoint(
        tap = tap,
        imageSize = imageSize,
        containerSize = containerSize,
        zoomScale = zoomScale,
        panOffset = panOffset,
    ) ?: return null

    return predictions.withIndex()
        .mapNotNull { (index, prediction) ->
            normalizedImageBox(
                x = prediction.box.x,
                y = prediction.box.y,
                w = prediction.box.w,
                h = prediction.box.h,
                imageSize = imageSize,
            )?.takeIf { box -> box.contains(imageTap.x.toDouble(), imageTap.y.toDouble()) }
                ?.let { box -> index to box.area }
        }
        .minByOrNull { (_, area) -> area }
        ?.first
}

internal fun overlayRectForImageBox(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    imageSize: IntSize,
    containerSize: IntSize,
    zoomScale: Float,
    panOffset: Offset,
): Rect? {
    val imageBox = normalizedImageBox(x, y, w, h, imageSize) ?: return null

    val topLeft = imagePointToOverlayPoint(
        imageX = imageBox.left,
        imageY = imageBox.top,
        imageSize = imageSize,
        containerSize = containerSize,
        zoomScale = zoomScale,
        panOffset = panOffset,
    ) ?: return null

    val bottomRight = imagePointToOverlayPoint(
        imageX = imageBox.right,
        imageY = imageBox.bottom,
        imageSize = imageSize,
        containerSize = containerSize,
        zoomScale = zoomScale,
        panOffset = panOffset,
    ) ?: return null

    if (!topLeft.isFiniteOffset() || !bottomRight.isFiniteOffset()) return null
    if (bottomRight.x <= topLeft.x || bottomRight.y <= topLeft.y) return null

    return Rect(topLeft, bottomRight)
}

internal fun overlayTapToImagePoint(
    tap: Offset,
    imageSize: IntSize,
    containerSize: IntSize,
    zoomScale: Float,
    panOffset: Offset,
): Offset? {
    if (!tap.isFiniteOffset()) return null
    val transform = imageTransform(imageSize, containerSize, zoomScale, panOffset) ?: return null

    val basePoint = Offset(
        x = ((tap.x - transform.center.x - transform.panOffset.x) / transform.zoomScale) + transform.center.x,
        y = ((tap.y - transform.center.y - transform.panOffset.y) / transform.zoomScale) + transform.center.y,
    )
    if (!basePoint.isFiniteOffset()) return null

    return Offset(
        x = ((basePoint.x - transform.imageOffset.x) / transform.baseScaleX),
        y = ((basePoint.y - transform.imageOffset.y) / transform.baseScaleY),
    ).takeIf { it.isFiniteOffset() }
}

internal fun imagePointToOverlayPoint(
    imageX: Double,
    imageY: Double,
    imageSize: IntSize,
    containerSize: IntSize,
    zoomScale: Float,
    panOffset: Offset,
): Offset? {
    if (!imageX.isFinite() || !imageY.isFinite()) return null
    val transform = imageTransform(imageSize, containerSize, zoomScale, panOffset) ?: return null

    val basePoint = Offset(
        x = transform.imageOffset.x + (imageX * transform.baseScaleX).toFloat(),
        y = transform.imageOffset.y + (imageY * transform.baseScaleY).toFloat(),
    )
    if (!basePoint.isFiniteOffset()) return null

    return Offset(
        x = transform.center.x + (basePoint.x - transform.center.x) * transform.zoomScale + transform.panOffset.x,
        y = transform.center.y + (basePoint.y - transform.center.y) * transform.zoomScale + transform.panOffset.y,
    ).takeIf { it.isFiniteOffset() }
}

private data class NormalizedImageBox(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double,
) {
    val area: Double get() = (right - left) * (bottom - top)

    fun contains(x: Double, y: Double): Boolean =
        x >= left && x <= right && y >= top && y <= bottom
}

private data class ImageTransform(
    val baseScaleX: Float,
    val baseScaleY: Float,
    val imageOffset: Offset,
    val center: Offset,
    val zoomScale: Float,
    val panOffset: Offset,
)

private fun normalizedImageBox(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    imageSize: IntSize,
): NormalizedImageBox? {
    if (!x.isFinite() || !y.isFinite() || !w.isFinite() || !h.isFinite()) return null
    if (w <= 0.0 || h <= 0.0) return null
    if (imageSize.width <= 0 || imageSize.height <= 0) return null

    val imageWidth = imageSize.width.toDouble()
    val imageHeight = imageSize.height.toDouble()
    val left = max(0.0, x)
    val top = max(0.0, y)
    val right = min(imageWidth, x + w)
    val bottom = min(imageHeight, y + h)

    if (!left.isFinite() || !top.isFinite() || !right.isFinite() || !bottom.isFinite()) return null
    if (right <= left || bottom <= top) return null

    return NormalizedImageBox(left, top, right, bottom)
}

private fun imageTransform(
    imageSize: IntSize,
    containerSize: IntSize,
    zoomScale: Float,
    panOffset: Offset,
): ImageTransform? {
    if (imageSize.width <= 0 || imageSize.height <= 0) return null
    if (containerSize.width <= 0 || containerSize.height <= 0) return null
    if (!zoomScale.isFinite() || zoomScale <= 0f) return null
    if (!panOffset.isFiniteOffset()) return null

    val imageWidth = imageSize.width.toFloat()
    val imageHeight = imageSize.height.toFloat()
    val containerWidth = containerSize.width.toFloat()
    val containerHeight = containerSize.height.toFloat()

    val baseScaleX = containerWidth / imageWidth
    val baseScaleY = containerHeight / imageHeight
    if (!baseScaleX.isFinite() || !baseScaleY.isFinite() || baseScaleX <= 0f || baseScaleY <= 0f) return null

    return ImageTransform(
        baseScaleX = baseScaleX,
        baseScaleY = baseScaleY,
        imageOffset = Offset.Zero,
        center = Offset(containerWidth / 2f, containerHeight / 2f),
        zoomScale = zoomScale,
        panOffset = panOffset,
    )
}

private fun Offset.isFiniteOffset(): Boolean = x.isFinite() && y.isFinite()


