package org.example.project.feature.auth.presentation

import org.example.project.common.presentation.ui.helpers.UiText

enum class AuthMode {
    SignIn,
    SignUp,
}

data class AuthContract(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null,
)

sealed interface AuthEvent {
    data class EmailChanged(val value: String) : AuthEvent
    data class PasswordChanged(val value: String) : AuthEvent
    data class FirstNameChanged(val value: String) : AuthEvent
    data class LastNameChanged(val value: String) : AuthEvent
    data object ToggleModeRequested : AuthEvent
    data object TogglePasswordVisibilityRequested : AuthEvent
    data object SubmitRequested : AuthEvent
    data object DismissErrorRequested : AuthEvent
}
