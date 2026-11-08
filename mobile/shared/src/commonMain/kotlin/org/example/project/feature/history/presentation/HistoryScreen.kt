package org.example.project.feature.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.common.presentation.ui.helpers.asString
import org.example.project.common.presentation.ui.theme.ScriptLensColors
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing
import org.example.project.common.presentation.ui.theme.ScriptLensTheme
import org.example.project.feature.history.domain.model.RecognitionHistoryItem
import org.example.project.feature.history.presentation.composables.GuestHistoryPrompt
import org.example.project.feature.history.presentation.composables.HistoryErrorCard
import org.example.project.feature.history.presentation.composables.HistoryListItem
import org.example.project.feature.history.presentation.composables.UnauthorizedHistoryCard
import org.jetbrains.compose.resources.stringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.history_action_read_page
import scriptlens.shared.generated.resources.history_authenticated_placeholder
import scriptlens.shared.generated.resources.history_count_result
import scriptlens.shared.generated.resources.history_count_results
import scriptlens.shared.generated.resources.history_empty_title
import scriptlens.shared.generated.resources.history_header_subtitle
import scriptlens.shared.generated.resources.history_header_title

@Composable
fun HistoryScreen(
    isAuthenticated: Boolean,
    viewModel: HistoryViewModel,
    onSignInRequested: () -> Unit,
    onCreateAccountRequested: () -> Unit,
    onReadPageRequested: () -> Unit,
    onOpenDetail: (Long) -> Unit,
) {
    if (!isAuthenticated) {
        GuestHistoryPrompt(
            onSignInRequested = onSignInRequested,
            onCreateAccountRequested = onCreateAccountRequested,
        )
        return
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.startObservers()
        viewModel.onEvent(HistoryEvent.LoadRequested)
    }

    HistoryListScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onOpenDetail = onOpenDetail,
        onSignInRequested = onSignInRequested,
        onReadPageRequested = onReadPageRequested,
    )
}

@Composable
private fun HistoryListScreen(
    state: HistoryContract,
    onEvent: (HistoryEvent) -> Unit,
    onOpenDetail: (Long) -> Unit,
    onSignInRequested: () -> Unit,
    onReadPageRequested: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScriptLensColors.Background)
            .imePadding()
            .padding(ScriptLensSpacing.padding_24),
        verticalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_16),
    ) {
        Text(
            text = stringResource(Res.string.history_header_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(Res.string.history_header_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when {
            state.isLoading && state.items.isEmpty() -> HistorySkeleton()
            state.requiresSignIn -> UnauthorizedHistoryCard(onSignInRequested)
            state.error != null && state.items.isEmpty() -> HistoryErrorCard(
                message = state.error.asString(),
                onRetry = { onEvent(HistoryEvent.RetryRequested) },
            )

            state.items.isEmpty() -> HistoryEmptyCard(onReadPageRequested)
            else -> {
                val countText = if (state.items.size == 1) {
                    stringResource(Res.string.history_count_result, state.items.size)
                } else {
                    stringResource(Res.string.history_count_results, state.items.size)
                }
                Text(
                    text = countText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_8),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        HistoryListItem(item = item, onClick = { onOpenDetail(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEmptyCard(onReadPageRequested: () -> Unit) {
    Card {
        Column(
            modifier = Modifier.padding(ScriptLensSpacing.padding_16),
            verticalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_12),
        ) {
            Text(
                text = stringResource(Res.string.history_empty_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.history_authenticated_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onReadPageRequested) {
                Text(text = stringResource(Res.string.history_action_read_page))
            }
        }
    }
}

@Composable
private fun HistorySkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_8)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ScriptLensSpacing.padding_56 + ScriptLensSpacing.padding_12)
                    .clip(RoundedCornerShape(ScriptLensSpacing.padding_12))
                    .background(ScriptLensColors.Muted)
                    .border(
                        1.dp,
                        ScriptLensColors.Border,
                        RoundedCornerShape(ScriptLensSpacing.padding_12)
                    ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    ScriptLensTheme {
        HistoryListScreen(
            state = HistoryContract(
                items = listOf(
                    RecognitionHistoryItem(
                        id = 1,
                        createdAt = "2026-01-15T10:30:00Z",
                        filename = "page.jpg",
                        modelVersion = "1.0",
                        previewText = "Example text",
                    ),
                ),
            ),
            onEvent = {},
            onOpenDetail = {},
            onSignInRequested = {},
            onReadPageRequested = {},
        )
    }
}
