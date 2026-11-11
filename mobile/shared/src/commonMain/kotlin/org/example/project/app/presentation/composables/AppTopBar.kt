package org.example.project.app.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.common.presentation.ui.composables.pill.ScriptLensPill
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.common.presentation.ui.theme.ScriptLensColors
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing
import org.example.project.common.presentation.ui.theme.ScriptLensTheme
import org.jetbrains.compose.resources.stringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.auth_action_sign_in
import scriptlens.shared.generated.resources.auth_action_sign_out
import scriptlens.shared.generated.resources.navigation_back

@Composable
internal fun AppTopBar(
    isAuthenticated: Boolean,
    isSigningOut: Boolean,
    showSessionActions: Boolean,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    onSignInRequested: () -> Unit,
    onSignOutRequested: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = ScriptLensSpacing.padding_16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (canNavigateBack) {
            TextButton(onClick = onBack) {
                Text(
                    stringResource(Res.string.navigation_back),
                    color = ScriptLensColors.Subtext
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (showSessionActions && isAuthenticated) {
            ScriptLensPill(
                isEnabled = !isSigningOut,
                onClick = onSignOutRequested,
                text = UiText.Plain(stringResource(Res.string.auth_action_sign_out))
            )
        } else if (showSessionActions) {
            ScriptLensPill(
                onClick = onSignInRequested,
                text = UiText.Plain(stringResource(Res.string.auth_action_sign_in))
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    ScriptLensTheme {
        AppTopBar(
            isAuthenticated = false,
            isSigningOut = false,
            showSessionActions = true,
            canNavigateBack = true,
            onBack = {},
            onSignInRequested = {},
            onSignOutRequested = {},
        )
    }
}
