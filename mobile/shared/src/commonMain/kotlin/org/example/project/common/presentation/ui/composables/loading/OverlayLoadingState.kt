package org.example.project.common.presentation.ui.composables.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.common.presentation.ui.theme.LexisColors

@Composable
fun OverlayLoadingState() {
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
        CircularProgressIndicator(
            color = LexisColors.Accent,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OverlayLoadingStatePreview() {
    MaterialTheme {
        OverlayLoadingState()
    }
}
