package org.example.project.common.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import org.example.project.core.persistence.ThemePersistence

private fun colorScheme(palette: LexisPalette, dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = palette.accent,
        onPrimary = palette.onAccent,
        background = palette.background,
        onBackground = palette.text,
        surface = palette.surface,
        onSurface = palette.text,
        surfaceVariant = palette.muted,
        onSurfaceVariant = palette.subtext,
        surfaceContainer = palette.surface,
        surfaceContainerLow = palette.surface,
        surfaceContainerHigh = palette.muted,
        outline = palette.border,
        outlineVariant = palette.border,
        scrim = palette.scrim,
    )
}

private val LexisShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** Whether the app is dark, and how to switch it. Like the web, a switch is saved and wins over the system. */
class LexisThemeState(val isDark: Boolean, val toggle: () -> Unit)

val LocalLexisThemeState = staticCompositionLocalOf { LexisThemeState(isDark = false, toggle = {}) }

/**
 * The app theme. [darkTheme] fixes the theme (previews, tests); left null, the app
 * follows the saved choice, or the system when nothing is saved.
 */
@Composable
fun LexisTheme(darkTheme: Boolean? = null, content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    var saved by remember { mutableStateOf(if (darkTheme == null) ThemePersistence.read() else null) }
    val isDark = darkTheme ?: saved ?: systemDark
    val state = LexisThemeState(isDark) {
        val next = !isDark
        saved = next
        ThemePersistence.write(next)
    }
    val palette = if (isDark) DarkPalette else LightPalette

    SystemBarsAppearance(isDark)
    CompositionLocalProvider(
        LocalLexisPalette provides palette,
        LocalLexisThemeState provides state,
    ) {
        MaterialTheme(
            colorScheme = colorScheme(palette, isDark),
            typography = LexisMaterialTypography,
            shapes = LexisShapes,
            content = content,
        )
    }
}

/** Makes the status bar text readable on the current theme. */
@Composable
internal expect fun SystemBarsAppearance(isDark: Boolean)
