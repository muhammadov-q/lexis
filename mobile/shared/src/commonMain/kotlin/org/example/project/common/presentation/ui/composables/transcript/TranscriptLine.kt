package org.example.project.common.presentation.ui.composables.transcript

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.models.PredictionLine
import org.example.project.common.presentation.helpers.groupPredictionLines
import org.example.project.common.presentation.ui.preview.previewTranslatedPrediction
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TranscriptLine(
    line: PredictionLine,
    showTranslation: Boolean,
    selectedPredictionIndex: Int?,
    onPredictionSelected: (Int?) -> Unit,
) {
    val lineTranslation = line.translation?.takeIf { showTranslation }
    val showWordGlosses = showTranslation && lineTranslation == null

    Row {
        Text(
            text = "${line.number + 1}",
            style = LexisTypography.Annotation,
            modifier = Modifier.width(28.dp).padding(top = 6.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4),
                verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4),
            ) {
                line.words.forEach { (index, word) ->
                    val isSelected = index == selectedPredictionIndex
                    TranscriptWord(
                        word = word,
                        isSelected = isSelected,
                        showGloss = showWordGlosses,
                        onClick = { onPredictionSelected(if (isSelected) null else index) },
                    )
                }
            }
            lineTranslation?.let {
                Text(text = it, style = LexisTypography.Body, color = LexisColors.Subtext)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TranscriptLinePreview() {
    MaterialTheme {
        TranscriptLine(
            line = groupPredictionLines(previewTranslatedPrediction.predictions).first(),
            showTranslation = true,
            selectedPredictionIndex = 1,
            onPredictionSelected = {},
        )
    }
}
