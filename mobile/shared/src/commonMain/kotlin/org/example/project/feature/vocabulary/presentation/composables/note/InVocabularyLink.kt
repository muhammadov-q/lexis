package org.example.project.feature.vocabulary.presentation.composables.note

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_in_vocabulary
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InVocabularyLink(onClick: () -> Unit) {
    Text(
        text = stringResource(Res.string.vocabulary_in_vocabulary),
        style = LexisTypography.BodySmall.copy(textDecoration = TextDecoration.Underline),
        color = LexisColors.Subtext,
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
    )
}

@Preview(showBackground = true)
@Composable
private fun InVocabularyLinkPreview() {
    LexisTheme {
        InVocabularyLink(onClick = {})
    }
}
