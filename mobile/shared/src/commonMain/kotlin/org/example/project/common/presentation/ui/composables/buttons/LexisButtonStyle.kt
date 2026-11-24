package org.example.project.common.presentation.ui.composables.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.LexisColors

enum class LexisButtonStyle(
    val containerColor: Color,
    val textColor: Color,
    val borderStroke: BorderStroke
) {
    Primary(
        containerColor = LexisColors.Accent,
        textColor = LexisColors.OnAccent,
        borderStroke = BorderStroke(0.dp, Color.Transparent)
    ),
    Secondary(
        containerColor = LexisColors.Surface,
        textColor = LexisColors.Subtext,
        borderStroke = BorderStroke(1.dp, LexisColors.Border)
    ),
}