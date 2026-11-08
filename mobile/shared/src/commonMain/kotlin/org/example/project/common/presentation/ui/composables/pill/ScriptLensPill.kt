package org.example.project.common.presentation.ui.composables.pill

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import org.example.project.common.presentation.ui.theme.ScriptLensColors
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing

@Composable
fun ScriptLensPill(
    modifier: Modifier=Modifier,
    text: UiText
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(16.dp))
            .background(ScriptLensColors.Accent).padding(
                horizontal = ScriptLensSpacing.padding_8,
                vertical = ScriptLensSpacing.padding_4
            )
    ) {
        Text(
            text = text.asString(),
            color = ScriptLensColors.Subtext
        )
    }
}

@Preview
@Composable
fun ScriptLensPillPreview() {
    ScriptLensPill(text = UiText.Plain("Preview"))
}