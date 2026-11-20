package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.word_loupe_description
import org.example.project.common.presentation.helpers.LOUPE_ASPECT
import org.example.project.common.presentation.helpers.loupeCrop
import org.example.project.common.presentation.models.WordPredictionModel
import org.example.project.common.presentation.ui.preview.previewPage
import org.example.project.common.presentation.ui.preview.previewTranslatedPrediction
import org.example.project.common.presentation.ui.theme.LexisColors
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun WordLoupe(image: ImageBitmap, word: WordPredictionModel) {
    val crop = loupeCrop(word.box, image.width, image.height)
    val srcOffset = IntOffset(crop.x.roundToInt(), crop.y.roundToInt())
    val srcSize = IntSize(
        crop.w.roundToInt().coerceIn(1, image.width - srcOffset.x),
        crop.h.roundToInt().coerceIn(1, image.height - srcOffset.y),
    )
    val description = stringResource(Res.string.word_loupe_description, word.text)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(LOUPE_ASPECT)
            .clip(MaterialTheme.shapes.large)
            .background(LexisColors.Muted)
            .semantics { contentDescription = description },
    ) {
        drawImage(
            image = image,
            srcOffset = srcOffset,
            srcSize = srcSize,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
        )

        val scaleX = size.width / srcSize.width
        val scaleY = size.height / srcSize.height
        drawRoundRect(
            color = LexisColors.Accent,
            topLeft = Offset(
                x = ((word.box.x - srcOffset.x) * scaleX).toFloat(),
                y = ((word.box.y - srcOffset.y) * scaleY).toFloat(),
            ),
            size = Size((word.box.w * scaleX).toFloat(), (word.box.h * scaleY).toFloat()),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WordLoupePreview() {
    MaterialTheme {
        WordLoupe(image = previewPage(), word = previewTranslatedPrediction.predictions[1])
    }
}
