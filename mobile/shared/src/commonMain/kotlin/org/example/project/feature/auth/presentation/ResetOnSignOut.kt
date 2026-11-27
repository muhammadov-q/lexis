package org.example.project.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.session.SessionMode

fun ViewModel.resetOnSignOut(sessionStore: AuthSessionStore, reset: () -> Unit) {
    var signedIn = sessionStore.isAuthenticated()
    viewModelScope.launch {
        sessionStore.mode.collect { mode ->
            when (mode) {
                is SessionMode.Authenticated -> signedIn = true
                SessionMode.Guest -> if (signedIn) {
                    signedIn = false
                    reset()
                }
            }
        }
    }
}
