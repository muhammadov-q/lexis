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
import org.example.project.core.network.ApiConfig
import org.example.project.core.network.ApiException
import org.example.project.core.network.apiException
import org.example.project.core.network.apiJson
import org.example.project.feature.auth.data.network.models.SignInRequestDto
import org.example.project.feature.auth.data.network.models.SignUpRequestDto
import org.example.project.feature.auth.data.network.models.UserDto

class AuthApi(
    private val client: HttpClient,
    private val config: ApiConfig,
) {

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
                    apiJson.encodeToString(
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
                    apiJson.encodeToString(
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
        return apiJson.decodeFromString(response.body)
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
            throw ApiException(ApiException.NO_RESPONSE)
        }
    }

    private fun endpoint(path: String): String = config.url("auth/$path")

    private data class RawResponse(
        val status: Int,
        val body: String,
    )
}
