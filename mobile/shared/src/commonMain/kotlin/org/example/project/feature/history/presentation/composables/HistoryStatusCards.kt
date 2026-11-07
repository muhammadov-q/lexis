package org.example.project.feature.history.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing
import org.jetbrains.compose.resources.stringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.history_error_title
import scriptlens.shared.generated.resources.history_guest_message
import scriptlens.shared.generated.resources.history_retry
import scriptlens.shared.generated.resources.history_sign_in

@Composable
internal fun HistoryErrorCard(message: String, onRetry: () -> Unit) {
    Card {
        Column(
            modifier = Modifier.padding(ScriptLensSpacing.padding_16),
            verticalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_12),
        ) {
            Text(
                text = stringResource(Res.string.history_error_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(Res.string.history_retry))
            }
        }
    }
}

@Composable
internal fun UnauthorizedHistoryCard(onSignInRequested: () -> Unit) {
    Card {
        Column(
            modifier = Modifier.padding(ScriptLensSpacing.padding_16),
            verticalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_12),
        ) {
            Text(
                text = stringResource(Res.string.history_sign_in),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.history_guest_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onSignInRequested) {
                Text(text = stringResource(Res.string.history_sign_in))
            }
        }
    }
}
