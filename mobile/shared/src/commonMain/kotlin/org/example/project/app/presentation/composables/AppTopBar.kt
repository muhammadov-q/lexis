package org.example.project.app.presentation.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.common.presentation.ui.composables.pill.LexisPill
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.auth_action_sign_in
import lexis.shared.generated.resources.auth_action_sign_out
import lexis.shared.generated.resources.auth_branding
import lexis.shared.generated.resources.ic_lexis_logo
import lexis.shared.generated.resources.navigation_back

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
            .padding(horizontal = LexisSpacing.padding_16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (canNavigateBack) {
            TextButton(onClick = onBack) {
                Text(
                    stringResource(Res.string.navigation_back),
                    color = LexisColors.Subtext
                )
            }
        } else {
            AppBrand()
        }
        Spacer(Modifier.weight(1f))
        if (showSessionActions && isAuthenticated) {
            LexisPill(
                isEnabled = !isSigningOut,
                onClick = onSignOutRequested,
                text = UiText.Plain(stringResource(Res.string.auth_action_sign_out))
            )
        } else if (showSessionActions) {
            LexisPill(
                onClick = onSignInRequested,
                text = UiText.Plain(stringResource(Res.string.auth_action_sign_in))
            )
        }
    }

}

/** The logo and the app name, on the left of the main screens. */
@Composable
private fun AppBrand() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_8),
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_lexis_logo),
            contentDescription = null,
            modifier = Modifier.size(LexisSpacing.padding_28),
        )
        Text(
            text = stringResource(Res.string.auth_branding),
            style = MaterialTheme.typography.titleMedium,
            color = LexisColors.Text,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    LexisTheme {
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
