package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography

private const val MISSING_VALUE = "—"

@Composable
internal fun GlossEntry(term: String, value: String, style: TextStyle) {
    Row(horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_16)) {
        Text(
            text = term,
            style = LexisTypography.BodySmall,
            color = LexisColors.Subtext,
            modifier = Modifier.width(112.dp).padding(top = 2.dp),
        )
        Text(
            text = value.ifBlank { MISSING_VALUE },
            style = style,
            color = LexisColors.Text,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GlossEntryPreview() {
    MaterialTheme {
        GlossEntry(term = "Translation", value = "difficult questions", style = LexisTypography.Subtitle)
    }
}
