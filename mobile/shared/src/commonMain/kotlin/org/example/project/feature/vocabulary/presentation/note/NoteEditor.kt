package org.example.project.feature.vocabulary.presentation.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_note_label
import lexis.shared.generated.resources.vocabulary_note_placeholder
import lexis.shared.generated.resources.vocabulary_saved
import lexis.shared.generated.resources.vocabulary_saving
import org.example.project.common.presentation.ui.composables.buttons.LexisButton
import org.example.project.common.presentation.ui.composables.buttons.LexisButtonSize
import org.example.project.common.presentation.ui.composables.buttons.LexisButtonStyle
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.feature.vocabulary.domain.model.MAX_NOTE_LENGTH
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NoteEditor(
    draft: String,
    onDraftChange: (String) -> Unit,
    buttonText: String,
    canSave: Boolean,
    isSaving: Boolean,
    isSaved: Boolean,
    error: String?,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    aside: @Composable () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_12),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.vocabulary_note_label),
                style = LexisTypography.Label,
                color = LexisColors.Text,
            )
            aside()
        }

        OutlinedTextField(
            value = draft,
            onValueChange = { onDraftChange(it.take(MAX_NOTE_LENGTH)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
            placeholder = { Text(stringResource(Res.string.vocabulary_note_placeholder)) },
            textStyle = LexisTypography.Body.copy(color = LexisColors.Text),
            shape = MaterialTheme.shapes.medium,
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LexisColors.Muted,
                unfocusedContainerColor = LexisColors.Muted,
                focusedBorderColor = LexisColors.Text,
                unfocusedBorderColor = LexisColors.Border,
                cursorColor = LexisColors.Text,
            ),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LexisButton(
                text = if (isSaving) stringResource(Res.string.vocabulary_saving) else buttonText,
                onClick = {
                    focusManager.clearFocus()
                    onSave()
                },
                enabled = canSave && !isSaving,
                buttonStyle = LexisButtonStyle.Primary,
                buttonSize = LexisButtonSize.Medium,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (isSaved) {
                Text(
                    text = stringResource(Res.string.vocabulary_saved),
                    style = LexisTypography.BodySmall,
                    color = LexisColors.Subtext,
                )
            }
        }

        error?.let {
            Text(text = it, style = LexisTypography.BodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteEditorPreview() {
    LexisTheme {
        NoteEditor(
            draft = "Remember the rough breathing.",
            onDraftChange = {},
            buttonText = "Save note",
            canSave = true,
            isSaving = false,
            isSaved = true,
            error = null,
            onSave = {},
        )
    }
}
