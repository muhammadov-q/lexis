package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography

@Composable
internal fun MutedNote(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(LexisColors.Muted)
            .padding(horizontal = LexisSpacing.padding_20, vertical = LexisSpacing.padding_16),
        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4),
    ) {
        Text(text = title, style = LexisTypography.Subtitle, color = LexisColors.Text)
        Text(text = message, style = LexisTypography.BodySmall, color = LexisColors.Subtext)
    }
}

@Preview(showBackground = true)
@Composable
private fun MutedNotePreview() {
    MaterialTheme {
        MutedNote(
            title = "No translation yet",
            message = "The model has no translation for this word.",
        )
    }
}
