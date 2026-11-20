package org.example.project.feature.camera.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.reading_cancel
import lexis.shared.generated.resources.reading_elapsed
import lexis.shared.generated.resources.reading_page
import org.example.project.common.presentation.helpers.formatDuration
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ReadingProgressOverlay(
    startedAtMs: Long,
    onCancel: () -> Unit,
) {
    var elapsedMs by remember(startedAtMs) { mutableLongStateOf(0L) }
    LaunchedEffect(startedAtMs) {
        while (true) {
            elapsedMs = Clock.System.now().toEpochMilliseconds() - startedAtMs
            delay(100)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LexisColors.Scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .padding(LexisSpacing.padding_24)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(LexisColors.Surface)
                .padding(LexisSpacing.padding_20),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_16),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = LexisColors.Accent,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.reading_page),
                    style = LexisTypography.Subtitle,
                    color = LexisColors.Text,
                )
                Text(
                    text = stringResource(Res.string.reading_elapsed, formatDuration(elapsedMs)),
                    style = LexisTypography.Annotation,
                )
            }
            Text(
                text = stringResource(Res.string.reading_cancel),
                style = LexisTypography.Label,
                color = LexisColors.Text,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .clickable(role = Role.Button, onClick = onCancel)
                    .padding(horizontal = LexisSpacing.padding_12, vertical = LexisSpacing.padding_8),
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview(showBackground = true)
@Composable
private fun ReadingProgressOverlayPreview() {
    MaterialTheme {
        ReadingProgressOverlay(
            startedAtMs = Clock.System.now().toEpochMilliseconds(),
            onCancel = {},
        )
    }
}
