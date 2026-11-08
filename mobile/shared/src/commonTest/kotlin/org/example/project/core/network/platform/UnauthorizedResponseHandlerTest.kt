package org.example.project.core.network.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.session.SessionMode
import kotlin.test.Test
import kotlin.test.assertTrue

class UnauthorizedResponseHandlerTest {
    @Test
    fun unauthorizedResponseSetsSessionToGuest() = runTest {
        val sessionStore = AuthSessionStore().apply {
            setAuthenticated(User(1, "user@example.com", null, null))
        }
        val client = HttpClient(MockEngine {
            respond(content = "", status = HttpStatusCode.Unauthorized)
        }) {
            handleUnauthorizedResponses(sessionStore)
        }

        client.get("https://example.com/")

        assertTrue(sessionStore.mode.value is SessionMode.Guest)
        client.close()
    }
}
