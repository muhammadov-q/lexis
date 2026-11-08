package org.example.project.core.network.platform

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import org.example.project.feature.auth.domain.session.AuthSessionStore

internal fun <T : HttpClientEngineConfig> HttpClientConfig<T>.handleUnauthorizedResponses(
    authSessionStore: AuthSessionStore,
) {
    HttpResponseValidator {
        validateResponse { response: HttpResponse ->
            if (response.status == HttpStatusCode.Unauthorized) {
                authSessionStore.setGuest()
            }
        }
    }
}
