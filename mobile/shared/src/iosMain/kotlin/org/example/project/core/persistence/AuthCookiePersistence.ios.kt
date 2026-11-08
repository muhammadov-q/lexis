package org.example.project.core.persistence

import platform.Foundation.NSUserDefaults

private const val AUTH_COOKIE_KEY = "session_cookies"

actual object AuthCookiePersistence : CookiePersistence {
    actual override fun read(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(AUTH_COOKIE_KEY)

    actual override fun write(value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, AUTH_COOKIE_KEY)
    }

    actual override fun clear() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(AUTH_COOKIE_KEY)
    }
}

