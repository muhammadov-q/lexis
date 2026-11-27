package org.example.project.core.persistence

/** The theme the user picked: true for dark, false for light, null to follow the system. */
expect object ThemePersistence {
    fun read(): Boolean?
    fun write(isDark: Boolean)
}
