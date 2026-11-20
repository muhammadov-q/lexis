package org.example.project.common.presentation.ui.composables.reading

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography

@Composable
internal fun ReadingCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(LexisColors.Surface)
            .padding(horizontal = LexisSpacing.padding_20, vertical = LexisSpacing.padding_20),
        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_20),
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun ReadingCardPreview() {
    MaterialTheme {
        ReadingCard {
            Text(text = "Transcript")
            Text(text = "137 words on 16 lines", style = LexisTypography.Annotation)
        }
    }
}
