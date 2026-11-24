package org.example.project.common.presentation.ui.composables.transcript

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.example.project.common.presentation.ui.preview.previewTranslatedPrediction
import org.example.project.common.presentation.ui.preview.previewUntranslatedPrediction
import org.example.project.common.presentation.models.WordPredictionModel

data class TranscriptCardPreviewState(
    val words: List<WordPredictionModel>,
    val selectedPredictionIndex: Int?,
)

class TranscriptCardPreviewProvider : PreviewParameterProvider<TranscriptCardPreviewState> {
    override val values = sequenceOf(
        TranscriptCardPreviewState(previewTranslatedPrediction.predictions, selectedPredictionIndex = 1),
        TranscriptCardPreviewState(previewUntranslatedPrediction.predictions, selectedPredictionIndex = null),
        TranscriptCardPreviewState(emptyList(), selectedPredictionIndex = null),
    )
}
