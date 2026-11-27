package org.example.project.core.persistence

import android.content.SharedPreferences
import org.example.project.core.platform.AndroidPlatformContext

private const val THEME_PREFS = "lexis_settings"
private const val THEME_KEY = "theme"
private const val DARK = "dark"
private const val LIGHT = "light"

actual object ThemePersistence {
    actual fun read(): Boolean? = when (prefs()?.getString(THEME_KEY, null)) {
        DARK -> true
        LIGHT -> false
        else -> null
    }

    actual fun write(isDark: Boolean) {
        prefs()?.edit()?.putString(THEME_KEY, if (isDark) DARK else LIGHT)?.apply()
    }

    // No context in previews and tests: fall back to the system theme there.
    private fun prefs(): SharedPreferences? =
        runCatching { AndroidPlatformContext.requireContext().getSharedPreferences(THEME_PREFS, 0) }.getOrNull()
}
