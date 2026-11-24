package org.example.project.common.presentation.ui.composables.pill

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.common.presentation.ui.helpers.asString
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing

@Composable
fun LexisPill(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    onClick: (() -> Unit),
    text: UiText
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = isEnabled) {
                onClick()
            }
            .background(
                if (isEnabled)
                    LexisColors.Accent
                else
                    LexisColors.Muted
            )
            .padding(
                horizontal = LexisSpacing.padding_8,
                vertical = LexisSpacing.padding_4
            )
    ) {
        Text(
            text = text.asString(),
            color = if (isEnabled)
                LexisColors.Subtext
            else
                LexisColors.Subtext.copy(alpha = 0.5f)
        )
    }
}

@Preview
@Composable
fun LexisPillPreview() {
    Row {
        LexisPill(
            text = UiText.Plain("Preview"),
            onClick = {},
            isEnabled = false
        )
        LexisPill(
            text = UiText.Plain("Preview"),
            onClick = {},
            isEnabled = true
        )
    }
}