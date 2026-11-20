package org.example.project.core.network.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import org.example.project.feature.auth.domain.session.AuthSessionStore

actual fun createHttpClient(
    cookieStorage: CookiesStorage,
    authSessionStore: AuthSessionStore,
): HttpClient =
    HttpClient(CIO) {
        expectSuccess = false

        install(HttpTimeout) {
            requestTimeoutMillis = 360_000 // 6 minutes
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 360_000
        }

        install(HttpCookies) {
            storage = cookieStorage
        }

        handleUnauthorizedResponses(authSessionStore)
    }

actual fun localBackendBaseUrl(): String = "http://127.0.0.1:8000"