package org.example.project.feature.vocabulary.presentation.word

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_error_word_title
import org.example.project.common.presentation.ui.helpers.asString
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.feature.vocabulary.presentation.composables.common.VocabularyErrorCard
import org.example.project.feature.vocabulary.presentation.composables.common.VocabularySignInPrompt
import org.example.project.feature.vocabulary.presentation.composables.common.previewVocabularyWord
import org.example.project.feature.vocabulary.presentation.composables.word.RemoveWordDialog
import org.example.project.feature.vocabulary.presentation.composables.word.VocabularyWordEntry
import org.jetbrains.compose.resources.stringResource

@Composable
fun VocabularyWordScreen(
    id: Long,
    isAuthenticated: Boolean,
    viewModel: VocabularyWordViewModel,
    onSignInRequested: () -> Unit,
    onCreateAccountRequested: () -> Unit,
    onOpenSource: (recognitionId: Long, wordIndex: Int) -> Unit,
    onRemoved: (word: String) -> Unit,
) {
    if (!isAuthenticated) {
        VocabularySignInPrompt(onSignInRequested, onCreateAccountRequested)
        return
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, id) { viewModel.onEvent(VocabularyWordEvent.LoadRequested(id)) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is VocabularyWordEffect.Removed -> onRemoved(effect.word)
            }
        }
    }

    VocabularyWordContent(
        state = state,
        onEvent = viewModel::onEvent,
        onRetry = { viewModel.onEvent(VocabularyWordEvent.LoadRequested(id)) },
        onSignInRequested = onSignInRequested,
        onCreateAccountRequested = onCreateAccountRequested,
        onOpenSource = onOpenSource,
    )
}

@Composable
private fun VocabularyWordContent(
    state: VocabularyWordContract,
    onEvent: (VocabularyWordEvent) -> Unit,
    onRetry: () -> Unit,
    onSignInRequested: () -> Unit,
    onCreateAccountRequested: () -> Unit,
    onOpenSource: (Long, Int) -> Unit,
) {
    if (state.requiresSignIn) {
        VocabularySignInPrompt(onSignInRequested, onCreateAccountRequested)
        return
    }

    val word = state.word
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexisColors.Background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(LexisSpacing.padding_24),
        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_16),
    ) {
        when {
            word != null -> VocabularyWordEntry(
                word = word,
                isSavingNote = state.isSavingNote,
                isNoteSaved = state.isNoteSaved,
                noteError = state.noteError?.asString(),
                onNoteEdited = { onEvent(VocabularyWordEvent.NoteEdited) },
                onSaveNote = { onEvent(VocabularyWordEvent.SaveNoteRequested(it)) },
                onRemove = { onEvent(VocabularyWordEvent.RemoveRequested) },
                onOpenSource = { onOpenSource(word.source.recognitionId, word.source.wordIndex) },
            )

            state.error != null -> VocabularyErrorCard(
                title = stringResource(Res.string.vocabulary_error_word_title),
                message = state.error.asString(),
                onRetry = onRetry,
            )

            else -> Box(Modifier.fillMaxWidth().padding(top = 96.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    if (word != null && state.isRemoveDialogShown) {
        RemoveWordDialog(
            word = word,
            isRemoving = state.isRemoving,
            error = state.removeError?.asString(),
            onConfirm = { onEvent(VocabularyWordEvent.RemoveConfirmed) },
            onDismiss = { onEvent(VocabularyWordEvent.RemoveDismissed) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VocabularyWordPreview() {
    LexisTheme {
        VocabularyWordContent(
            state = VocabularyWordContract(word = previewVocabularyWord),
            onEvent = {},
            onRetry = {},
            onSignInRequested = {},
            onCreateAccountRequested = {},
            onOpenSource = { _, _ -> },
        )
    }
}
