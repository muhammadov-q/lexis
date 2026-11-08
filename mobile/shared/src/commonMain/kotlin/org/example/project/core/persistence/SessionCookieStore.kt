package org.example.project.core.persistence

interface SessionCookieStore {
    suspend fun clearSessionCookie()
}

