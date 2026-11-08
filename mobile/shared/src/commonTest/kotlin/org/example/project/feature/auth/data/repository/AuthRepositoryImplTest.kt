package org.example.project.feature.auth.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.example.project.core.persistence.SessionCookieStore
import org.example.project.feature.auth.data.network.AuthApi
import org.example.project.feature.auth.data.network.AuthConfig
import org.example.project.feature.auth.domain.model.AuthApiException
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.session.SessionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {
    private val config = AuthConfig(baseUrl = "http://localhost:8000")

    @Test
    fun signInReturnsUser() = runTest {
        val sessionStore = AuthSessionStore()
        val engine = MockEngine { request ->
            assertEquals("/auth/sign-in", request.url.encodedPath)
            respond(
                content = """{"id":1,"email":"user@example.com","first_name":"John","last_name":"Doe"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val repository = repositoryWithEngine(engine, sessionStore = sessionStore)

        val result = repository.signIn("user@example.com", "password123")

        assertEquals(1, result.id)
        assertEquals("user@example.com", result.email)
        assertEquals("John", result.firstName)
        assertEquals("Doe", result.lastName)
        assertTrue(sessionStore.mode.value is SessionMode.Authenticated)
    }

    @Test
    fun signUpPropagatesConflictAsApiException() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"detail":"An account with this email already exists. Sign in instead."}""",
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val repository = repositoryWithEngine(engine)

        val exception = assertFailsWith<AuthApiException> {
            repository.signUp("user@example.com", "password123", "John", "Doe")
        }

        assertEquals(409, exception.status)
    }

    @Test
    fun currentUserOrNullReturnsNullWithoutClearingCookieOnUnauthorized() = runTest {
        val cookieStore = FakeCookieStore()
        val sessionStore = AuthSessionStore()
        sessionStore.setAuthenticated(User(1, "user@example.com", null, null))
        val engine = MockEngine {
            respond(
                content = """{"detail":"Sign in to continue."}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val repository = repositoryWithEngine(engine, cookieStore, sessionStore)

        val result = repository.currentUserOrNull()

        assertNull(result)
        assertEquals(0, cookieStore.clearCount)
        assertTrue(sessionStore.mode.value is SessionMode.Authenticated)
    }

    @Test
    fun signOutClearsSessionCookieAfterSuccess() = runTest {
        val cookieStore = FakeCookieStore()
        val sessionStore = AuthSessionStore()
        val engine = MockEngine {
            respond(
                content = "",
                status = HttpStatusCode.NoContent,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val repository = repositoryWithEngine(engine, cookieStore, sessionStore)

        repository.signOut()

        assertEquals(1, cookieStore.clearCount)
        assertTrue(sessionStore.mode.value is SessionMode.Guest)
    }

    @Test
    fun signOutClearsLocalSessionWhenBackendRequestFails() = runTest {
        val cookieStore = FakeCookieStore()
        val sessionStore = AuthSessionStore()
        sessionStore.setAuthenticated(User(1, "user@example.com", null, null))
        val engine = MockEngine {
            respond(content = "", status = HttpStatusCode.InternalServerError)
        }
        val repository = repositoryWithEngine(engine, cookieStore, sessionStore)

        assertFailsWith<AuthApiException> { repository.signOut() }

        assertEquals(1, cookieStore.clearCount)
        assertTrue(sessionStore.mode.value is SessionMode.Guest)
    }

    private fun repositoryWithEngine(
        engine: MockEngine,
        cookieStore: FakeCookieStore = FakeCookieStore(),
        sessionStore: AuthSessionStore = AuthSessionStore(),
    ): AuthRepositoryImpl {
        val api = AuthApi(HttpClient(engine), config)
        return AuthRepositoryImpl(
            api = api,
            sessionCookieStore = cookieStore,
            sessionStore = sessionStore,
        )
    }

    private class FakeCookieStore : SessionCookieStore {
        var clearCount: Int = 0

        override suspend fun clearSessionCookie() {
            clearCount += 1
        }
    }
}
