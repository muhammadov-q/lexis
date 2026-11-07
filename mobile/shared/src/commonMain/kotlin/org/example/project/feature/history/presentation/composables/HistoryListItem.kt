package org.example.project.feature.history.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.common.presentation.ui.theme.ScriptLensColors
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing
import org.example.project.common.presentation.ui.theme.ScriptLensTheme
import org.example.project.feature.history.domain.model.RecognitionHistoryItem
import org.example.project.feature.history.presentation.formatHistoryDateTime
import org.jetbrains.compose.resources.stringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.history_read_on

@Composable
internal fun HistoryListItem(
    item: RecognitionHistoryItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = ScriptLensColors.Muted),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScriptLensSpacing.padding_12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = ScriptLensSpacing.padding_12),
                verticalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_4),
            ) {
                Text(
                    text = if (item.filename.isBlank()) "Untitled page" else item.filename,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        Res.string.history_read_on,
                        formatHistoryDateTime(item.createdAt)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = if (item.previewText.isBlank()) "No words" else item.previewText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryListItemPreview() {
    ScriptLensTheme {
        HistoryListItem(
            item = RecognitionHistoryItem(
                id = 1,
                createdAt = "2026-01-15T10:30:00Z",
                filename = "page.jpg",
                modelVersion = "1.0",
                previewText = "Example text",
            ),
            onClick = {},
        )
    }
}
