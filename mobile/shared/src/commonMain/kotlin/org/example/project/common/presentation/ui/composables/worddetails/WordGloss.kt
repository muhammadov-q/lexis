package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.word_confidence
import lexis.shared.generated.resources.word_definition
import lexis.shared.generated.resources.word_dictionary_form
import lexis.shared.generated.resources.word_not_confirmed
import lexis.shared.generated.resources.word_translation
import org.example.project.common.presentation.ui.preview.previewTranslatedPrediction
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.common.presentation.helpers.confidence
import org.example.project.common.presentation.helpers.formatPercent
import org.example.project.common.presentation.models.WordPredictionModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WordGloss(word: WordPredictionModel) {
    val confidence = word.confidence()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(LexisColors.Muted)
            .padding(horizontal = LexisSpacing.padding_20, vertical = LexisSpacing.padding_16),
        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_12),
    ) {
        GlossEntry(
            term = stringResource(Res.string.word_translation),
            value = word.gloss.translation,
            style = LexisTypography.Subtitle.copy(fontSize = 18.sp),
        )
        GlossEntry(
            term = stringResource(Res.string.word_dictionary_form),
            value = word.gloss.lemma,
            style = LexisTypography.Body.copy(fontSize = 18.sp),
        )
        GlossEntry(
            term = stringResource(Res.string.word_definition),
            value = word.gloss.definition,
            style = LexisTypography.Body,
        )
        GlossEntry(
            term = stringResource(Res.string.word_confidence),
            value = confidence?.let(::formatPercent) ?: stringResource(Res.string.word_not_confirmed),
            style = if (confidence == null) {
                LexisTypography.BodySmall
            } else {
                LexisTypography.Annotation.copy(fontSize = 14.sp, color = LexisColors.Text)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WordGlossPreview() {
    MaterialTheme {
        WordGloss(word = previewTranslatedPrediction.predictions.first())
    }
}
