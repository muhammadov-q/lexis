package org.example.project.feature.vocabulary.presentation.composables.word

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_cancel
import lexis.shared.generated.resources.vocabulary_remove
import lexis.shared.generated.resources.vocabulary_remove_note_deleted
import lexis.shared.generated.resources.vocabulary_remove_title
import lexis.shared.generated.resources.vocabulary_removing
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.feature.vocabulary.domain.model.VocabularyWord
import org.example.project.feature.vocabulary.presentation.composables.common.previewVocabularyWord
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RemoveWordDialog(
    word: VocabularyWord,
    isRemoving: Boolean,
    error: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isRemoving) onDismiss() },
        containerColor = LexisColors.Surface,
        titleContentColor = LexisColors.Text,
        textContentColor = LexisColors.Subtext,
        title = { Text(stringResource(Res.string.vocabulary_remove_title, word.word)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_8)) {
                if (word.note.isNotBlank()) {
                    Text(stringResource(Res.string.vocabulary_remove_note_deleted))
                }
                error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = !isRemoving, onClick = onConfirm) {
                Text(
                    text = stringResource(
                        if (isRemoving) Res.string.vocabulary_removing else Res.string.vocabulary_remove
                    ),
                    style = LexisTypography.Label,
                    color = LexisColors.Text,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.vocabulary_cancel),
                    style = LexisTypography.Label,
                    color = LexisColors.Subtext,
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun RemoveWordDialogPreview() {
    LexisTheme {
        RemoveWordDialog(
            word = previewVocabularyWord,
            isRemoving = false,
            error = null,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
