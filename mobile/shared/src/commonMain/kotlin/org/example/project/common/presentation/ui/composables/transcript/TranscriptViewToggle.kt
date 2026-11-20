package org.example.project.common.presentation.ui.composables.transcript

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
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
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.transcript_view_original
import lexis.shared.generated.resources.transcript_view_translation
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TranscriptViewToggle(withTranslation: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(LexisColors.Muted)
            .padding(4.dp),
    ) {
        ToggleOption(
            text = stringResource(Res.string.transcript_view_original),
            selected = !withTranslation,
            onClick = { onChange(false) },
        )
        ToggleOption(
            text = stringResource(Res.string.transcript_view_translation),
            selected = withTranslation,
            onClick = { onChange(true) },
        )
    }
}

@Composable
private fun ToggleOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        style = LexisTypography.Label.copy(fontSize = 12.sp),
        color = if (selected) LexisColors.Text else LexisColors.Subtext,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) LexisColors.Surface else LexisColors.Muted)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun TranscriptViewTogglePreview() {
    MaterialTheme {
        TranscriptViewToggle(withTranslation = true, onChange = {})
    }
}
