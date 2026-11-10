package org.example.project.core.network.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import org.example.project.feature.auth.domain.session.AuthSessionStore

actual fun createHttpClient(
    cookieStorage: CookiesStorage,
    authSessionStore: AuthSessionStore,
): HttpClient =
    HttpClient(Darwin) {
        expectSuccess = false
        install(HttpCookies) {
            storage = cookieStorage
        }
        handleUnauthorizedResponses(authSessionStore)
    }

actual fun localBackendBaseUrl(): String = "http://134.21.144.107:8000"
