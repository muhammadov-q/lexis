package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.word_next
import lexis.shared.generated.resources.word_position
import lexis.shared.generated.resources.word_previous
import org.example.project.common.presentation.ui.preview.previewTranslatedPrediction
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.common.presentation.helpers.positionOf
import org.example.project.common.presentation.models.WordPredictionModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WordHeading(
    words: List<WordPredictionModel>,
    index: Int,
    onPredictionSelected: (Int?) -> Unit,
) {
    val position = positionOf(words, index)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_16),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_8),
        ) {
            Text(
                text = words[index].text,
                style = LexisTypography.Title.copy(fontWeight = FontWeight.Medium, lineHeight = 36.sp),
                color = LexisColors.Text,
            )
            Text(
                text = stringResource(Res.string.word_position, position.line, position.word),
                style = LexisTypography.Annotation,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4)) {
            WordStepButton(
                direction = StepDirection.Previous,
                description = stringResource(Res.string.word_previous),
                enabled = index > 0,
                onClick = { onPredictionSelected(index - 1) },
            )
            WordStepButton(
                direction = StepDirection.Next,
                description = stringResource(Res.string.word_next),
                enabled = index < words.lastIndex,
                onClick = { onPredictionSelected(index + 1) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WordHeadingPreview() {
    MaterialTheme {
        WordHeading(
            words = previewTranslatedPrediction.predictions,
            index = 0,
            onPredictionSelected = {},
        )
    }
}
