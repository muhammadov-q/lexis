package org.example.project.common.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LexisColorScheme = lightColorScheme(
    primary = LexisColors.Accent,
    onPrimary = LexisColors.OnAccent,
    background = LexisColors.Background,
    onBackground = LexisColors.Text,
    surface = LexisColors.Surface,
    onSurface = LexisColors.Text,
    surfaceVariant = LexisColors.Muted,
    onSurfaceVariant = LexisColors.Subtext,
    outline = LexisColors.Border,
    scrim = LexisColors.Scrim,
)

private val LexisShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun LexisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LexisColorScheme,
        typography = LexisMaterialTypography,
        shapes = LexisShapes,
        content = content,
    )
}
