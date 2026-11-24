package org.example.project.feature.vocabulary.presentation.note

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_save_note
import lexis.shared.generated.resources.vocabulary_save_word
import org.example.project.common.presentation.ui.helpers.asString
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.feature.vocabulary.domain.model.headword
import org.example.project.feature.vocabulary.presentation.composables.common.previewVocabularyWords
import org.example.project.feature.vocabulary.presentation.composables.note.InVocabularyLink
import org.example.project.feature.vocabulary.presentation.composables.note.WordNoteSkeleton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WordNote(
    recognitionId: Long,
    wordIndex: Int,
    text: String,
    onOpenVocabularyWord: (Long) -> Unit,
    viewModel: WordNoteViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.onEvent(WordNoteEvent.LoadRequested) }
    LaunchedEffect(wordIndex) { viewModel.onEvent(WordNoteEvent.WordChanged) }

    WordNoteContent(
        state = state,
        wordIndex = wordIndex,
        text = text,
        onSave = { word, note ->
            viewModel.onEvent(
                WordNoteEvent.SaveRequested(
                    recognitionId = recognitionId,
                    wordIndex = wordIndex,
                    headword = word,
                    note = note,
                )
            )
        },
        onOpenVocabularyWord = onOpenVocabularyWord,
    )
}

@Composable
private fun WordNoteContent(
    state: WordNoteContract,
    wordIndex: Int,
    text: String,
    onSave: (headword: String, note: String) -> Unit,
    onOpenVocabularyWord: (Long) -> Unit,
) {
    val word = headword(text)
    val words = state.words
    if (word.isEmpty() || state.isLoadFailed) return
    if (words == null) {
        WordNoteSkeleton()
        return
    }

    val entry = words.find { it.word == word }
    var draft by rememberSaveable(wordIndex, entry?.id, entry?.note) { mutableStateOf(entry?.note.orEmpty()) }
    val changed = draft.trim() != entry?.note.orEmpty()

    NoteEditor(
        draft = draft,
        onDraftChange = { draft = it },
        buttonText = stringResource(
            if (entry == null) Res.string.vocabulary_save_word else Res.string.vocabulary_save_note
        ),
        canSave = entry == null || changed,
        isSaving = state.isSaving,
        isSaved = state.savedWord == word && !changed,
        error = state.error?.asString(),
        onSave = { onSave(word, draft) },
        aside = {
            if (entry != null) {
                InVocabularyLink(onClick = { onOpenVocabularyWord(entry.id) })
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun WordNoteContentPreview() {
    LexisTheme {
        WordNoteContent(
            state = WordNoteContract(words = previewVocabularyWords),
            wordIndex = 0,
            text = "λόγος,",
            onSave = { _, _ -> },
            onOpenVocabularyWord = {},
        )
    }
}
