package org.example.project.feature.vocabulary.presentation.composables.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.history_create_account
import lexis.shared.generated.resources.history_sign_in
import lexis.shared.generated.resources.vocabulary_guest_message
import lexis.shared.generated.resources.vocabulary_guest_title
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VocabularySignInPrompt(
    onSignInRequested: () -> Unit,
    onCreateAccountRequested: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexisColors.Background)
            .padding(LexisSpacing.padding_24),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.vocabulary_guest_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            modifier = Modifier.padding(top = LexisSpacing.padding_12),
            text = stringResource(Res.string.vocabulary_guest_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            modifier = Modifier.fillMaxWidth().padding(top = LexisSpacing.padding_24),
            onClick = onSignInRequested,
        ) {
            Text(text = stringResource(Res.string.history_sign_in))
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().padding(top = LexisSpacing.padding_12),
            onClick = onCreateAccountRequested,
        ) {
            Text(text = stringResource(Res.string.history_create_account))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VocabularySignInPromptPreview() {
    LexisTheme {
        VocabularySignInPrompt(onSignInRequested = {}, onCreateAccountRequested = {})
    }
}
