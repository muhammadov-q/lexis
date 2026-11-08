package org.example.project.core.persistence

import org.example.project.core.platform.AndroidPlatformContext

private const val AUTH_PREFS = "team4_auth"
private const val AUTH_COOKIE_KEY = "session_cookies"

actual object AuthCookiePersistence : CookiePersistence {
    actual override fun read(): String? {
        val prefs = AndroidPlatformContext.requireContext().getSharedPreferences(AUTH_PREFS, 0)
        return prefs.getString(AUTH_COOKIE_KEY, null)
    }

    actual override fun write(value: String) {
        val prefs = AndroidPlatformContext.requireContext().getSharedPreferences(AUTH_PREFS, 0)
        prefs.edit().putString(AUTH_COOKIE_KEY, value).apply()
    }

    actual override fun clear() {
        val prefs = AndroidPlatformContext.requireContext().getSharedPreferences(AUTH_PREFS, 0)
        prefs.edit().remove(AUTH_COOKIE_KEY).apply()
    }
}

