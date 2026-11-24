package org.example.project.feature.vocabulary.presentation.composables.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.history_untitled_page
import lexis.shared.generated.resources.vocabulary_source
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.feature.vocabulary.domain.model.WordSource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun sourceLabel(source: WordSource): String = stringResource(
    Res.string.vocabulary_source,
    source.filename.ifBlank { stringResource(Res.string.history_untitled_page) },
    source.line + 1,
)

@Preview(showBackground = true)
@Composable
private fun SourceLabelPreview() {
    LexisTheme {
        Text(text = sourceLabel(previewVocabularyWord.source))
    }
}
