package org.example.project.app.presentation

import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.feature.auth.presentation.AuthMode

data class AppContract(
    val isAuthenticated: Boolean = false,
    val isSessionChecking: Boolean = true,
    val isSigningOut: Boolean = false,
    val error: UiText? = null,
)

sealed interface AppEvent {
    data class SignInRequested(val mode: AuthMode) : AppEvent
    data object SignOutRequested : AppEvent
    data object GuestContinueRequested : AppEvent
    data object DismissErrorRequested : AppEvent
}

sealed interface AppEffect {
    data class NavigateToAuth(val mode: AuthMode) : AppEffect
    data object NavigateToDashboard : AppEffect
    data object NavigateToCameraRoot : AppEffect
}
