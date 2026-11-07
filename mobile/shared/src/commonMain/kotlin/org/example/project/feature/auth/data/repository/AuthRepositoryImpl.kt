package org.example.project.feature.auth.data.repository

import org.example.project.core.persistence.SessionCookieStore
import org.example.project.feature.auth.data.mapper.mapTo
import org.example.project.feature.auth.data.network.AuthApi
import org.example.project.feature.auth.domain.model.AuthApiException
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.repository.AuthRepository
import org.example.project.feature.auth.domain.session.AuthSessionStore

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val sessionCookieStore: SessionCookieStore,
    private val sessionStore: AuthSessionStore,
) : AuthRepository {
    override suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): User {
        val user = mapTo(
            api.signUp(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
            )
        )
        sessionStore.setAuthenticated(user)
        return user
    }

    override suspend fun signIn(email: String, password: String): User {
        val user = mapTo(api.signIn(email = email, password = password))
        sessionStore.setAuthenticated(user)
        return user
    }

    override suspend fun signOut() {
        try {
            api.signOut()
        } finally {
            sessionCookieStore.clearSessionCookie()
            sessionStore.setGuest()
        }
    }

    override suspend fun currentUserOrNull(): User? {
        return try {
            val user = mapTo(api.me())
            sessionStore.setAuthenticated(user)
            user
        } catch (exception: AuthApiException) {
            if (exception.status == 401) {
                sessionCookieStore.clearSessionCookie()
                sessionStore.setGuest()
                null
            } else {
                throw exception
            }
        }
    }
}
