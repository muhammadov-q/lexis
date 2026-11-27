package org.example.project.common.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** The colors of one theme. Light and dark follow the web app's tokens. */
data class LexisPalette(
    val accent: Color,
    val onAccent: Color,
    val success: Color,
    val background: Color,
    val surface: Color,
    val muted: Color,
    val scrim: Color,
    val text: Color,
    val subtext: Color,
    val border: Color,
)

val LightPalette = LexisPalette(
    accent = Color(0xFFE6FF66),
    onAccent = Color(0xFF50582A),
    success = Color(0xFF8BC34A),
    background = Color(0xFFF4F4F4),
    surface = Color.White,
    muted = Color(0xFFEBECEC),
    scrim = Color(0x396A6A6A),
    text = Color.DarkGray,
    subtext = Color.Gray,
    border = Color.LightGray,
)

val DarkPalette = LexisPalette(
    accent = Color(0xFFE6FF5C),
    onAccent = Color(0xFF0E0E0E),
    success = Color(0xFFE6FF5C),
    background = Color(0xFF000000),
    surface = Color(0xFF161616),
    muted = Color(0xFF1F1F1F),
    scrim = Color(0x99000000),
    text = Color(0xFFF2F2F2),
    subtext = Color(0xFFA3A3A3),
    border = Color(0xFF333333),
)

internal val LocalLexisPalette = staticCompositionLocalOf { LightPalette }

/** The current theme's colors. Read them inside a composable, under [LexisTheme]. */
object LexisColors {
    // Brand
    val Accent: Color @Composable @ReadOnlyComposable get() = LocalLexisPalette.current.accent
    val OnAccent: Color @Composable @ReadOnlyComposable get() = LocalLexisPalette.current.onAccent
    val Success: Color @Composable @ReadOnlyComposable get() = LocalLexisPalette.current.success

    // Surfaces
    val Background: Color @Composable @ReadOnlyComposable get() = LocalLexisPalette.current.background
    val Surface: Color @Composable @ReadOnlyComposable get() = LocalLexisPalette.current.surface
    val Muted: Color @Composable @ReadOnlyComposable get() = LocalLexisPalette.current.muted
    val Scrim: Color @Composable @ReadOnlyComposable get() = LocalLexisPalette.current.scrim

    // Text and lines
    val Text: Color @Composable @ReadOnlyComposable get() = LocalLexisPalette.current.text
    val Subtext: Color @Composable @ReadOnlyComposable get() = LocalLexisPalette.current.subtext
    val Border: Color @Composable @ReadOnlyComposable get() = LocalLexisPalette.current.border
}
