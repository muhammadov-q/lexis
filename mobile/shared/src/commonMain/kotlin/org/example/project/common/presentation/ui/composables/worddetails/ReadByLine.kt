package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.result_placeholder
import lexis.shared.generated.resources.result_read_by
import lexis.shared.generated.resources.result_read_in
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.helpers.formatDuration
import org.example.project.common.presentation.helpers.isPlaceholderModel
import org.jetbrains.compose.resources.stringResource
import org.example.project.common.presentation.ui.theme.LexisTypography

private val ModelStyle: SpanStyle
    @Composable get() = SpanStyle(fontFamily = FontFamily.Monospace, color = LexisColors.Text)

@Composable
internal fun ReadByLine(modelVersion: String, durationMs: Long?) {
    val readBy = stringResource(Res.string.result_read_by)
    val readIn = durationMs?.let { stringResource(Res.string.result_read_in, formatDuration(it)) }

    Column(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4)) {
        Text(
            text = buildAnnotatedString {
                append("$readBy ")
                withStyle(ModelStyle) { append(modelVersion) }
                readIn?.let { append(" $it") }
            },
            style = LexisTypography.BodySmall,
            color = LexisColors.Subtext,
        )
        if (isPlaceholderModel(modelVersion)) {
            Text(
                text = stringResource(Res.string.result_placeholder),
                style = LexisTypography.BodySmall,
                color = LexisColors.Subtext,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadByLinePreview() {
    MaterialTheme {
        ReadByLine(modelVersion = "claude-opus-5-5+kraken-kraken_eparchos", durationMs = 68_600)
    }
}
