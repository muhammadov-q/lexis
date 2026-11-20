package org.example.project.common.presentation.ui.composables.transcript

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.common.presentation.models.WordPredictionModel
import org.example.project.common.presentation.ui.preview.previewTranslatedPrediction
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisTypography

@Composable
internal fun TranscriptWord(
    word: WordPredictionModel,
    isSelected: Boolean,
    showGloss: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) LexisColors.Accent else LexisColors.Surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Text(
            text = word.text,
            style = LexisTypography.Body.copy(fontSize = 17.sp, lineHeight = 26.sp),
            color = if (isSelected) LexisColors.OnAccent else LexisColors.Text,
        )
        if (showGloss) {
            Text(
                text = word.gloss.translation.ifBlank { "·" },
                style = LexisTypography.Annotation,
                color = if (isSelected) LexisColors.OnAccent else LexisColors.Subtext,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TranscriptWordPreview() {
    MaterialTheme {
        TranscriptWord(
            word = previewTranslatedPrediction.predictions.first(),
            isSelected = true,
            showGloss = true,
            onClick = {},
        )
    }
}
