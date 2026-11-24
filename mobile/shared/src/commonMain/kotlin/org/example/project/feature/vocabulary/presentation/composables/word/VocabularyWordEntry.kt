package org.example.project.feature.vocabulary.presentation.composables.word

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_added_on
import lexis.shared.generated.resources.vocabulary_meaning
import lexis.shared.generated.resources.vocabulary_remove_word
import lexis.shared.generated.resources.vocabulary_save_note
import org.example.project.common.presentation.helpers.HistoryDateTimeFormatter
import org.example.project.common.presentation.ui.composables.reading.MachineOutputChip
import org.example.project.common.presentation.ui.composables.reading.ReadingCard
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.feature.vocabulary.domain.model.VocabularyWord
import org.example.project.feature.vocabulary.presentation.composables.common.previewVocabularyWord
import org.example.project.feature.vocabulary.presentation.note.NoteEditor
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VocabularyWordEntry(
    word: VocabularyWord,
    isSavingNote: Boolean,
    isNoteSaved: Boolean,
    noteError: String?,
    onNoteEdited: () -> Unit,
    onSaveNote: (String) -> Unit,
    onRemove: () -> Unit,
    onOpenSource: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_16)) {
        Column(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4)) {
            Text(
                text = word.word,
                style = LexisTypography.Title.copy(fontWeight = FontWeight.SemiBold),
                color = LexisColors.Text,
            )
            Text(
                text = stringResource(Res.string.vocabulary_added_on, HistoryDateTimeFormatter.format(word.createdAt)),
                style = LexisTypography.BodySmall,
                color = LexisColors.Subtext,
            )
        }

        ReadingCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.vocabulary_meaning),
                    style = LexisTypography.Annotation.copy(fontSize = 14.sp),
                )
                MachineOutputChip()
            }
            WordMeaning(gloss = word.gloss)
            SourceLink(source = word.source, onClick = onOpenSource)
        }

        ReadingCard {
            var draft by rememberSaveable(word.id, word.note) { mutableStateOf(word.note) }
            val changed = draft.trim() != word.note

            NoteEditor(
                draft = draft,
                onDraftChange = {
                    draft = it
                    onNoteEdited()
                },
                buttonText = stringResource(Res.string.vocabulary_save_note),
                canSave = changed,
                isSaving = isSavingNote,
                isSaved = isNoteSaved && !changed,
                error = noteError,
                onSave = { onSaveNote(draft) },
            )

            TextButton(onClick = onRemove) {
                Text(
                    text = stringResource(Res.string.vocabulary_remove_word),
                    style = LexisTypography.Label,
                    color = LexisColors.Text,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VocabularyWordEntryPreview() {
    LexisTheme {
        VocabularyWordEntry(
            word = previewVocabularyWord,
            isSavingNote = false,
            isNoteSaved = false,
            noteError = null,
            onNoteEdited = {},
            onSaveNote = {},
            onRemove = {},
            onOpenSource = {},
        )
    }
}
