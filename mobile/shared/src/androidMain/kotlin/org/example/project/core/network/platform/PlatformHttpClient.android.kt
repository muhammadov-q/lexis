package org.example.project.core.network.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies

actual fun createHttpClient(cookieStorage: CookiesStorage): HttpClient =
    HttpClient(CIO) {
        expectSuccess = false
        install(HttpCookies) {
            storage = cookieStorage
        }
    }

actual fun localBackendBaseUrl(): String = "http://127.0.0.1:8000"
