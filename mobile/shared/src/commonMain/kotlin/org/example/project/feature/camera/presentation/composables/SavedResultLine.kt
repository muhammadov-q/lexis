package org.example.project.feature.camera.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.result_not_saved
import lexis.shared.generated.resources.result_open_it
import lexis.shared.generated.resources.result_saved
import lexis.shared.generated.resources.result_sign_in
import lexis.shared.generated.resources.result_sign_in_suffix
import org.example.project.common.presentation.ui.theme.LexisColors
import org.jetbrains.compose.resources.stringResource
import org.example.project.common.presentation.ui.theme.LexisTypography

private val LinkStyle = SpanStyle(
    color = LexisColors.Text,
    fontWeight = FontWeight.Medium,
    textDecoration = TextDecoration.Underline,
)

@Composable
fun SavedResultLine(
    savedId: Long?,
    isAuthenticated: Boolean,
    onOpenSaved: (Long) -> Unit,
    onSignInRequested: () -> Unit,
) {
    when {
        savedId != null -> LinkLine(
            prefix = stringResource(Res.string.result_saved),
            link = stringResource(Res.string.result_open_it),
            onClick = { onOpenSaved(savedId) },
        )

        !isAuthenticated -> LinkLine(
            prefix = stringResource(Res.string.result_not_saved),
            link = stringResource(Res.string.result_sign_in),
            suffix = stringResource(Res.string.result_sign_in_suffix),
            onClick = onSignInRequested,
        )
    }
}

@Composable
private fun LinkLine(prefix: String, link: String, onClick: () -> Unit, suffix: String? = null) {
    Text(
        text = buildAnnotatedString {
            append("$prefix ")
            withStyle(LinkStyle) { append(link) }
            suffix?.let { append(" $it") }
        },
        style = LexisTypography.BodySmall,
        color = LexisColors.Subtext,
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
    )
}

@Preview(showBackground = true)
@Composable
private fun SavedResultLinePreview() {
    MaterialTheme {
        SavedResultLine(
            savedId = null,
            isAuthenticated = false,
            onOpenSaved = {},
            onSignInRequested = {},
        )
    }
}
