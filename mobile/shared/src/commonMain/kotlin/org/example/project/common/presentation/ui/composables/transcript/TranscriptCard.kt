package org.example.project.common.presentation.ui.composables.transcript

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.transcript_empty
import org.example.project.common.presentation.helpers.groupPredictionLines
import org.example.project.common.presentation.helpers.isTranslated
import org.example.project.common.presentation.models.WordPredictionModel
import org.example.project.common.presentation.ui.composables.reading.ReadingCard
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.jetbrains.compose.resources.stringResource

@Composable
fun TranscriptCard(
    words: List<WordPredictionModel>,
    selectedPredictionIndex: Int?,
    onPredictionSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var withTranslation by rememberSaveable { mutableStateOf(false) }
    val lines = remember(words) { groupPredictionLines(words) }
    val translated = remember(words) { words.isTranslated() }

    ReadingCard(modifier = modifier) {
        TranscriptHeader(wordCount = words.size, lineCount = lines.size)

        if (translated) {
            TranscriptViewToggle(
                withTranslation = withTranslation,
                onChange = { withTranslation = it },
            )
        }

        if (lines.isEmpty()) {
            Text(
                text = stringResource(Res.string.transcript_empty),
                style = LexisTypography.Body,
                color = LexisColors.Subtext,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_12)) {
            lines.forEach { line ->
                TranscriptLine(
                    line = line,
                    showTranslation = translated && withTranslation,
                    selectedPredictionIndex = selectedPredictionIndex,
                    onPredictionSelected = onPredictionSelected,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TranscriptCardPreview(
    @PreviewParameter(TranscriptCardPreviewProvider::class) state: TranscriptCardPreviewState,
) {
    MaterialTheme {
        TranscriptCard(
            words = state.words,
            selectedPredictionIndex = state.selectedPredictionIndex,
            onPredictionSelected = {},
        )
    }
}
