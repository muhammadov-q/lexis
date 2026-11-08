package org.example.project.feature.auth.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.feature.auth.domain.model.User


class AuthSessionStore {
    private val _mode = MutableStateFlow<SessionMode>(SessionMode.Guest)
    val mode = _mode.asStateFlow()

    fun isAuthenticated(): Boolean = _mode.value is SessionMode.Authenticated

    fun setGuest() {
        _mode.value = SessionMode.Guest
    }

    fun setAuthenticated(user: User) {
        _mode.value = SessionMode.Authenticated(user)
    }
}
