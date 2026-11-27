package org.example.project.core.network

data class ApiConfig(val baseUrl: String) {
    fun url(path: String): String = "${baseUrl.trimEnd('/')}/${path.trimStart('/')}"
}
