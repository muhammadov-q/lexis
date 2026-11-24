package org.example.project.feature.vocabulary.presentation.composables.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_count_word
import lexis.shared.generated.resources.vocabulary_count_words
import lexis.shared.generated.resources.vocabulary_machine_translations
import org.example.project.common.presentation.ui.composables.reading.ReadingCard
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.feature.vocabulary.domain.model.VocabularyWord
import org.example.project.feature.vocabulary.presentation.composables.common.previewVocabularyWords
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VocabularyWordList(words: List<VocabularyWord>, onOpenWord: (Long) -> Unit) {
    ReadingCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (words.size == 1) {
                    stringResource(Res.string.vocabulary_count_word, words.size)
                } else {
                    stringResource(Res.string.vocabulary_count_words, words.size)
                },
                style = LexisTypography.Subtitle,
                color = LexisColors.Text,
            )
            Text(
                text = stringResource(Res.string.vocabulary_machine_translations),
                style = LexisTypography.Annotation,
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_8)) {
            items(words, key = { it.id }) { word ->
                VocabularyWordRow(word = word, onClick = { onOpenWord(word.id) })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VocabularyWordListPreview() {
    LexisTheme {
        VocabularyWordList(words = previewVocabularyWords, onOpenWord = {})
    }
}
