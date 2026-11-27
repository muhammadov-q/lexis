package org.example.project.common.presentation.ui.composables.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.LexisColors

enum class LexisButtonStyle {
    Primary,
    Secondary;

    val containerColor: Color
        @Composable get() = when (this) {
            Primary -> LexisColors.Accent
            Secondary -> LexisColors.Surface
        }

    val textColor: Color
        @Composable get() = when (this) {
            Primary -> LexisColors.OnAccent
            Secondary -> LexisColors.Subtext
        }

    val borderStroke: BorderStroke
        @Composable get() = when (this) {
            Primary -> BorderStroke(0.dp, Color.Transparent)
            Secondary -> BorderStroke(1.dp, LexisColors.Border)
        }
}
