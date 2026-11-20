package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.example.project.common.presentation.ui.preview.PREVIEW_UNCONFIRMED_WORD
import org.example.project.common.presentation.ui.preview.previewEmptyPrediction
import org.example.project.common.presentation.ui.preview.previewTranslatedPrediction
import org.example.project.common.presentation.ui.preview.previewUntranslatedPrediction
import org.example.project.common.presentation.models.PredictionModel

data class WordDetailsCardPreviewState(
    val prediction: PredictionModel,
    val selectedPredictionIndex: Int?,
    val durationMs: Long? = null,
)

class WordDetailsCardPreviewProvider : PreviewParameterProvider<WordDetailsCardPreviewState> {
    override val values = sequenceOf(
        WordDetailsCardPreviewState(
            prediction = previewTranslatedPrediction,
            selectedPredictionIndex = null,
            durationMs = 68_600,
        ),
        WordDetailsCardPreviewState(
            prediction = previewTranslatedPrediction,
            selectedPredictionIndex = 1,
            durationMs = 68_600,
        ),
        WordDetailsCardPreviewState(
            prediction = previewTranslatedPrediction,
            selectedPredictionIndex = PREVIEW_UNCONFIRMED_WORD,
        ),
        WordDetailsCardPreviewState(
            prediction = previewUntranslatedPrediction,
            selectedPredictionIndex = 0,
            durationMs = 850,
        ),
        WordDetailsCardPreviewState(
            prediction = previewEmptyPrediction,
            selectedPredictionIndex = null,
        ),
    )
}
