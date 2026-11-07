package org.example.project.core.network.platform

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cookies.CookiesStorage

expect fun createHttpClient(cookieStorage: CookiesStorage): HttpClient

expect fun localBackendBaseUrl(): String
