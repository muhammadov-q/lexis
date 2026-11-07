package org.example.project.feature.auth.domain.session

import org.example.project.feature.auth.domain.model.User

sealed interface SessionMode {
    data object Guest : SessionMode
    data class Authenticated(val user: User) : SessionMode
}

