package org.example.project.feature.vocabulary.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_error_title
import lexis.shared.generated.resources.vocabulary_header_subtitle
import lexis.shared.generated.resources.vocabulary_header_title
import org.example.project.common.presentation.ui.helpers.asString
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.feature.vocabulary.presentation.composables.common.VocabularyErrorCard
import org.example.project.feature.vocabulary.presentation.composables.common.VocabularySignInPrompt
import org.example.project.feature.vocabulary.presentation.composables.common.previewVocabularyWords
import org.example.project.feature.vocabulary.presentation.composables.list.VocabularyEmptyCard
import org.example.project.feature.vocabulary.presentation.composables.list.VocabularySkeleton
import org.example.project.feature.vocabulary.presentation.composables.list.VocabularyWordList
import org.jetbrains.compose.resources.stringResource

@Composable
fun VocabularyScreen(
    isAuthenticated: Boolean,
    viewModel: VocabularyViewModel,
    onSignInRequested: () -> Unit,
    onCreateAccountRequested: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenWord: (Long) -> Unit,
) {
    if (!isAuthenticated) {
        VocabularySignInPrompt(
            onSignInRequested = onSignInRequested,
            onCreateAccountRequested = onCreateAccountRequested,
        )
        return
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.onEvent(VocabularyEvent.LoadRequested) }

    VocabularyListContent(
        state = state,
        onRetry = { viewModel.onEvent(VocabularyEvent.RetryRequested) },
        onSignInRequested = onSignInRequested,
        onCreateAccountRequested = onCreateAccountRequested,
        onOpenHistory = onOpenHistory,
        onOpenWord = onOpenWord,
    )
}

@Composable
private fun VocabularyListContent(
    state: VocabularyContract,
    onRetry: () -> Unit,
    onSignInRequested: () -> Unit,
    onCreateAccountRequested: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenWord: (Long) -> Unit,
) {
    if (state.requiresSignIn) {
        VocabularySignInPrompt(onSignInRequested, onCreateAccountRequested)
        return
    }

    val words = state.words
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexisColors.Background)
            .padding(LexisSpacing.padding_24),
        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_16),
    ) {
        Text(
            text = stringResource(Res.string.vocabulary_header_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(Res.string.vocabulary_header_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when {
            words == null && state.error != null -> VocabularyErrorCard(
                title = stringResource(Res.string.vocabulary_error_title),
                message = state.error.asString(),
                onRetry = onRetry,
            )

            words == null -> VocabularySkeleton()
            words.isEmpty() -> VocabularyEmptyCard(onOpenHistory)
            else -> VocabularyWordList(words = words, onOpenWord = onOpenWord)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VocabularyListPreview() {
    LexisTheme {
        VocabularyListContent(
            state = VocabularyContract(words = previewVocabularyWords),
            onRetry = {},
            onSignInRequested = {},
            onCreateAccountRequested = {},
            onOpenHistory = {},
            onOpenWord = {},
        )
    }
}
