package org.example.project.feature.vocabulary.presentation.composables.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.history_retry
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VocabularyErrorCard(title: String, message: String, onRetry: (() -> Unit)? = null) {
    Card {
        Column(
            modifier = Modifier.padding(LexisSpacing.padding_16),
            verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_12),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            onRetry?.let {
                Button(onClick = it) { Text(text = stringResource(Res.string.history_retry)) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VocabularyErrorCardPreview() {
    LexisTheme {
        VocabularyErrorCard(
            title = "Could not load your vocabulary",
            message = "Could not reach the server. Check your network and try again.",
            onRetry = {},
        )
    }
}
