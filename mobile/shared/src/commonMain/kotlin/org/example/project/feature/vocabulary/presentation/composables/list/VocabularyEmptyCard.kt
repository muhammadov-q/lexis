package org.example.project.feature.vocabulary.presentation.composables.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_empty_message
import lexis.shared.generated.resources.vocabulary_empty_title
import lexis.shared.generated.resources.vocabulary_open_history
import org.example.project.common.presentation.ui.composables.buttons.LexisButton
import org.example.project.common.presentation.ui.composables.buttons.LexisButtonSize
import org.example.project.common.presentation.ui.composables.reading.ReadingCard
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VocabularyEmptyCard(onOpenHistory: () -> Unit) {
    ReadingCard {
        Column(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_8)) {
            Text(
                text = stringResource(Res.string.vocabulary_empty_title),
                style = LexisTypography.Subtitle,
                color = LexisColors.Text,
            )
            Text(
                text = stringResource(Res.string.vocabulary_empty_message),
                style = LexisTypography.BodySmall,
                color = LexisColors.Subtext,
            )
        }
        LexisButton(
            text = stringResource(Res.string.vocabulary_open_history),
            onClick = onOpenHistory,
            buttonSize = LexisButtonSize.Medium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VocabularyEmptyCardPreview() {
    LexisTheme {
        VocabularyEmptyCard(onOpenHistory = {})
    }
}
