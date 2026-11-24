package org.example.project.feature.historydetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.common.presentation.helpers.HistoryDateTimeFormatter
import org.example.project.common.presentation.ui.helpers.asString
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.composables.pagescan.PageScan
import org.example.project.feature.camera.presentation.helpers.decodePhoto
import org.example.project.feature.camera.presentation.mappers.mapPredictionToUiModel
import org.example.project.feature.history.presentation.composables.GuestHistoryPrompt
import org.example.project.feature.history.presentation.composables.HistoryErrorCard
import org.example.project.feature.history.presentation.composables.UnauthorizedHistoryCard
import org.jetbrains.compose.resources.stringResource
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.history_detail_image_unavailable
import lexis.shared.generated.resources.history_detail_read_on
import lexis.shared.generated.resources.history_untitled_page
import org.example.project.common.presentation.ui.composables.transcript.TranscriptCard
import org.example.project.common.presentation.ui.composables.worddetails.WordDetailsCard
import org.example.project.feature.vocabulary.presentation.note.WordNote

@Composable
fun HistoryDetailScreen(
    id: Long,
    isAuthenticated: Boolean,
    viewModel: HistoryDetailViewModel,
    onSignInRequested: () -> Unit,
    onCreateAccountRequested: () -> Unit,
    initialWordIndex: Int? = null,
    onOpenVocabularyWord: (Long) -> Unit = {},
) {
    if (!isAuthenticated) {
        GuestHistoryPrompt(
            onSignInRequested = onSignInRequested,
            onCreateAccountRequested = onCreateAccountRequested,
        )
        return
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, id, initialWordIndex) {
        viewModel.load(id, initialWordIndex)
    }

    Box(modifier = Modifier.fillMaxSize().background(LexisColors.Background)) {
        HistoryDetailContent(
            state = state,
            onRetry = { viewModel.load(id, initialWordIndex) },
            onPredictionSelected = viewModel::selectPrediction,
            onSignInRequested = onSignInRequested,
            onOpenVocabularyWord = onOpenVocabularyWord,
            scrollToSelectedWord = initialWordIndex != null,
        )
    }
}

@Composable
private fun HistoryDetailContent(
    state: HistoryDetailContract,
    onRetry: () -> Unit,
    onPredictionSelected: (Int?) -> Unit,
    onSignInRequested: () -> Unit,
    onOpenVocabularyWord: (Long) -> Unit,
    scrollToSelectedWord: Boolean = false,
) {
    if (state.requiresSignIn) {
        UnauthorizedHistoryCard(onSignInRequested)
        return
    }

    val detail = state.selectedDetail
    if (state.isLoading && detail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.error != null && detail == null) {
        HistoryErrorCard(message = state.error.asString(), onRetry = onRetry)
        return
    }

    detail ?: return
    val predictionModel = remember(detail.id) { mapPredictionToUiModel(detail.prediction) }
    val imageBitmap = remember(detail.id) { detail.imageBytes?.let(::decodePhoto) }
    val wordDetails = remember { BringIntoViewRequester() }

    LaunchedEffect(detail.id) {
        if (scrollToSelectedWord && state.selectedPredictionIndex != null) {
            wordDetails.bringIntoView()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(LexisSpacing.padding_24),
        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_12),
    ) {
        Text(
            text = if (detail.filename.isBlank()) {
                stringResource(Res.string.history_untitled_page)
            } else {
                detail.filename
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(
                Res.string.history_detail_read_on,
                HistoryDateTimeFormatter.format(detail.createdAt),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (imageBitmap == null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = LexisSpacing.padding_56 * 4)
                    .clip(RoundedCornerShape(LexisSpacing.padding_12)),
                color = LexisColors.Muted,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.history_detail_image_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            PageScan(
                image = imageBitmap,
                contentDescription = detail.filename,
                prediction = predictionModel,
                selectedPredictionIndex = state.selectedPredictionIndex,
                onPredictionSelected = onPredictionSelected,
            )
        }

        WordDetailsCard(
            modifier = Modifier.bringIntoViewRequester(wordDetails),
            prediction = predictionModel,
            selectedPredictionIndex = state.selectedPredictionIndex,
            onPredictionSelected = onPredictionSelected,
            image = imageBitmap,
            selectedWordFooter = { index, word ->
                WordNote(
                    recognitionId = detail.id,
                    wordIndex = index,
                    text = word.text,
                    onOpenVocabularyWord = onOpenVocabularyWord,
                )
            },
        )

        TranscriptCard(
            words = predictionModel.predictions,
            selectedPredictionIndex = state.selectedPredictionIndex,
            onPredictionSelected = onPredictionSelected,
        )
    }
}
