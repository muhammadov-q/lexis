package org.example.project.core.persistence

import platform.Foundation.NSUserDefaults

private const val THEME_KEY = "theme"
private const val DARK = "dark"
private const val LIGHT = "light"

actual object ThemePersistence {
    actual fun read(): Boolean? = when (NSUserDefaults.standardUserDefaults.stringForKey(THEME_KEY)) {
        DARK -> true
        LIGHT -> false
        else -> null
    }

    actual fun write(isDark: Boolean) {
        NSUserDefaults.standardUserDefaults.setObject(if (isDark) DARK else LIGHT, THEME_KEY)
    }
}
