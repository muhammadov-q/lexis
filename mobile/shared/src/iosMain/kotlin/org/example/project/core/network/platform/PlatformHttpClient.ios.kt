package org.example.project.core.network.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies

actual fun createHttpClient(cookieStorage: CookiesStorage): HttpClient =
    HttpClient(Darwin) {
        expectSuccess = false
        install(HttpCookies) {
            storage = cookieStorage
        }
    }

actual fun localBackendBaseUrl(): String = "http://192.168.205.88:8000"
