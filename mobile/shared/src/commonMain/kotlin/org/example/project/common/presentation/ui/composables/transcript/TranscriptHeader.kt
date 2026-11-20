package org.example.project.common.presentation.ui.composables.transcript

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.transcript_count
import lexis.shared.generated.resources.transcript_lines
import lexis.shared.generated.resources.transcript_title
import lexis.shared.generated.resources.transcript_words
import org.example.project.common.presentation.ui.composables.reading.MachineOutputChip
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TranscriptHeader(wordCount: Int, lineCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Text(
                text = stringResource(Res.string.transcript_title),
                style = LexisTypography.Subtitle,
                color = LexisColors.Text,
            )
            Text(
                text = stringResource(
                    Res.string.transcript_count,
                    pluralStringResource(Res.plurals.transcript_words, wordCount, wordCount),
                    pluralStringResource(Res.plurals.transcript_lines, lineCount, lineCount),
                ),
                style = LexisTypography.Annotation,
            )
        }
        MachineOutputChip()
    }
}

@Preview(showBackground = true)
@Composable
private fun TranscriptHeaderPreview() {
    MaterialTheme {
        TranscriptHeader(wordCount = 137, lineCount = 16)
    }
}
