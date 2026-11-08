package org.example.project.feature.auth.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import org.example.project.feature.auth.data.network.models.AuthErrorDto
import org.example.project.feature.auth.data.network.models.SignInRequestDto
import org.example.project.feature.auth.data.network.models.SignUpRequestDto
import org.example.project.feature.auth.data.network.models.UserDto
import org.example.project.feature.auth.domain.model.AuthApiException

class AuthApi(
    private val client: HttpClient,
    private val config: AuthConfig,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): UserDto {
        val endpoint = endpoint(AuthRoutes.SIGN_UP)
        val response = request {
            client.post(endpoint) {
                header(HttpHeaders.ContentType, "application/json")
                setBody(
                    json.encodeToString(
                        SignUpRequestDto(
                            email = email,
                            password = password,
                            firstName = firstName,
                            lastName = lastName,
                        )
                    )
                )
            }
        }
        return decodeUser(response, expectedStatus = HttpStatusCode.Created.value)
    }

    suspend fun signIn(email: String, password: String): UserDto {
        val endpoint = endpoint(AuthRoutes.SIGN_IN)
        val response = request {
            client.post(endpoint) {
                header(HttpHeaders.ContentType, "application/json")
                setBody(
                    json.encodeToString(
                        SignInRequestDto(email = email, password = password)
                    )
                )
            }
        }
        return decodeUser(response, expectedStatus = HttpStatusCode.OK.value)
    }

    suspend fun signOut() {
        val endpoint = endpoint(AuthRoutes.SIGN_OUT)
        val response = request { client.post(endpoint) }
        if (response.status != HttpStatusCode.NoContent.value) {
            throw apiException(status = response.status, body = response.body)
        }
    }

    suspend fun me(): UserDto {
        val endpoint = endpoint(AuthRoutes.ME)
        val response = request { client.get(endpoint) }
        return decodeUser(response, expectedStatus = HttpStatusCode.OK.value)
    }

    private fun decodeUser(response: RawResponse, expectedStatus: Int): UserDto {
        if (response.status != expectedStatus) {
            throw apiException(response.status, response.body)
        }
        return json.decodeFromString(response.body)
    }

    private suspend fun request(call: suspend () -> HttpResponse): RawResponse {
        return try {
            val response = call()
            RawResponse(
                status = response.status.value,
                body = response.bodyAsText(),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            throw AuthApiException(status = 0)
        }
    }

    private fun apiException(status: Int, body: String): AuthApiException {
        val detail = runCatching { json.decodeFromString<AuthErrorDto>(body).detail }.getOrNull()
        return AuthApiException(status = status, detail = detail)
    }

    private fun endpoint(path: String): String {
        val base = config.baseUrl.trimEnd('/')
        val auth = config.authPath.trim('/').let { if (it.isBlank()) "" else "/$it" }
        return "$base$auth/${path.trimStart('/')}"
    }

    private data class RawResponse(
        val status: Int,
        val body: String,
    )
}
