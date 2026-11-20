package org.example.project.common.presentation.ui.composables.pagescan

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.example.project.common.presentation.ui.preview.PREVIEW_UNCONFIRMED_WORD
import org.example.project.common.presentation.ui.preview.previewTranslatedPrediction
import org.example.project.common.presentation.models.PredictionModel

data class PageScanPreviewState(
    val prediction: PredictionModel?,
    val selectedPredictionIndex: Int?,
)

class PageScanPreviewProvider : PreviewParameterProvider<PageScanPreviewState> {
    override val values = sequenceOf(
        PageScanPreviewState(previewTranslatedPrediction, selectedPredictionIndex = PREVIEW_UNCONFIRMED_WORD),
        PageScanPreviewState(previewTranslatedPrediction, selectedPredictionIndex = null),
        PageScanPreviewState(prediction = null, selectedPredictionIndex = null),
    )
}
