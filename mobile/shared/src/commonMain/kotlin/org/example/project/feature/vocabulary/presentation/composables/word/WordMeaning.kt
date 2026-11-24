package org.example.project.feature.vocabulary.presentation.composables.word

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_no_translation
import lexis.shared.generated.resources.word_definition
import lexis.shared.generated.resources.word_dictionary_form
import lexis.shared.generated.resources.word_translation
import org.example.project.common.presentation.ui.composables.worddetails.GlossEntry
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.feature.vocabulary.domain.model.VocabularyGloss
import org.example.project.feature.vocabulary.presentation.composables.common.previewVocabularyWord
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WordMeaning(gloss: VocabularyGloss?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(LexisColors.Muted)
            .padding(horizontal = LexisSpacing.padding_20, vertical = LexisSpacing.padding_16),
        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_12),
    ) {
        if (gloss == null) {
            Text(
                text = stringResource(Res.string.vocabulary_no_translation),
                style = LexisTypography.Subtitle,
                color = LexisColors.Text,
            )
        } else {
            GlossEntry(
                term = stringResource(Res.string.word_translation),
                value = gloss.translation,
                style = LexisTypography.Subtitle.copy(fontSize = 18.sp),
            )
            GlossEntry(
                term = stringResource(Res.string.word_dictionary_form),
                value = gloss.lemma,
                style = LexisTypography.Body.copy(fontSize = 18.sp),
            )
            GlossEntry(
                term = stringResource(Res.string.word_definition),
                value = gloss.definition,
                style = LexisTypography.Body,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WordMeaningPreview() {
    LexisTheme {
        WordMeaning(gloss = previewVocabularyWord.gloss)
    }
}
