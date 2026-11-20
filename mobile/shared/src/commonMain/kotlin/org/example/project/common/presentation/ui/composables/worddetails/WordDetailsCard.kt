package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.result_no_words_message
import lexis.shared.generated.resources.result_no_words_title
import org.example.project.common.presentation.ui.composables.reading.ReadingCard
import org.example.project.common.presentation.models.PredictionModel
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.graphics.ImageBitmap
import org.example.project.common.presentation.ui.preview.previewPage

@Composable
fun WordDetailsCard(
    prediction: PredictionModel,
    selectedPredictionIndex: Int?,
    onPredictionSelected: (Int?) -> Unit,
    image: ImageBitmap?,
    modifier: Modifier = Modifier,
    durationMs: Long? = null,
    footer: @Composable () -> Unit = {},
) {
    val words = prediction.predictions
    val selected = selectedPredictionIndex?.takeIf { it in words.indices }

    ReadingCard(modifier = modifier) {
        WordDetailsHeader()

        when {
            words.isEmpty() -> MutedNote(
                title = stringResource(Res.string.result_no_words_title),
                message = stringResource(Res.string.result_no_words_message),
            )

            selected == null -> PickAWordPlaceholder()

            else -> SelectedWord(
                words = words,
                index = selected,
                image = image,
                onPredictionSelected = onPredictionSelected,
            )
        }

        ReadByLine(modelVersion = prediction.modelVersion, durationMs = durationMs)

        footer()
    }
}

@Preview(showBackground = true)
@Composable
private fun WordDetailsCardPreview(
    @PreviewParameter(WordDetailsCardPreviewProvider::class) state: WordDetailsCardPreviewState,
) {
    MaterialTheme {
        WordDetailsCard(
            prediction = state.prediction,
            selectedPredictionIndex = state.selectedPredictionIndex,
            onPredictionSelected = {},
            image = previewPage(),
            durationMs = state.durationMs,
        )
    }
}
