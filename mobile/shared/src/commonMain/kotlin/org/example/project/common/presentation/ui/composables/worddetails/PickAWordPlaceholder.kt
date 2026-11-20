package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.word_pick_message
import lexis.shared.generated.resources.word_pick_title
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.aspectRatio
import org.example.project.common.presentation.helpers.LOUPE_ASPECT

@Composable
internal fun PickAWordPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(LexisColors.Muted)
            .aspectRatio(LOUPE_ASPECT)
            .padding(horizontal = LexisSpacing.padding_24),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.word_pick_title),
                style = LexisTypography.Subtitle,
                color = LexisColors.Text,
            )
            Text(
                text = stringResource(Res.string.word_pick_message),
                style = LexisTypography.BodySmall,
                color = LexisColors.Subtext,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PickAWordPlaceholderPreview() {
    MaterialTheme {
        PickAWordPlaceholder()
    }
}
