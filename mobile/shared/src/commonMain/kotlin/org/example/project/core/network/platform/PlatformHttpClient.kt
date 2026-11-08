package org.example.project.core.network.platform

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cookies.CookiesStorage
import org.example.project.feature.auth.domain.session.AuthSessionStore

expect fun createHttpClient(
    cookieStorage: CookiesStorage,
    authSessionStore: AuthSessionStore,
): HttpClient

expect fun localBackendBaseUrl(): String
