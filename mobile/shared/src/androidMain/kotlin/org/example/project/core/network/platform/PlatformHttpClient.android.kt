package org.example.project.core.network.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import org.example.project.core.network.BackendConfig
import org.example.project.feature.auth.domain.session.AuthSessionStore

actual fun createHttpClient(
    cookieStorage: CookiesStorage,
    authSessionStore: AuthSessionStore,
): HttpClient =
    HttpClient(CIO) {
        expectSuccess = false

        install(HttpTimeout)

        install(HttpCookies) {
            storage = cookieStorage
        }

        handleUnauthorizedResponses(authSessionStore)
    }

actual fun localBackendBaseUrl(): String = BackendConfig.ANDROID_URL
