package org.example.project.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.core.mvi.setState
import org.example.project.feature.auth.domain.usecase.SignInUseCase
import org.example.project.feature.auth.domain.usecase.SignUpUseCase
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.auth_error_password_min_length
import scriptlens.shared.generated.resources.auth_error_validation

private const val MIN_SIGN_UP_PASSWORD_LENGTH = 8

class AuthViewModel(
    private val signIn: SignInUseCase,
    private val signUp: SignUpUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthContract())
    val state = _state.asStateFlow()
    private var initialModeSet = false

    fun initializeMode(mode: AuthMode) {
        if (initialModeSet) return
        initialModeSet = true
        _state.setState { it.copy(mode = mode, error = null) }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> _state.setState { it.copy(email = event.value, error = null) }
            is AuthEvent.PasswordChanged -> _state.setState { it.copy(password = event.value, error = null) }
            is AuthEvent.FirstNameChanged -> _state.setState { it.copy(firstName = event.value, error = null) }
            is AuthEvent.LastNameChanged -> _state.setState { it.copy(lastName = event.value, error = null) }
            is AuthEvent.ToggleModeRequested -> toggleMode()
            is AuthEvent.TogglePasswordVisibilityRequested -> {
                _state.setState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is AuthEvent.SubmitRequested -> submit()
            is AuthEvent.DismissErrorRequested -> _state.setState { it.copy(error = null) }
        }
    }

    private fun toggleMode() {
        _state.setState {
            it.copy(
                mode = if (it.mode == AuthMode.SignIn) AuthMode.SignUp else AuthMode.SignIn,
                error = null,
            )
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isLoading) return

        if (current.email.isBlank() || current.password.isBlank()) {
            _state.setState { it.copy(error = UiText.Resource(Res.string.auth_error_validation)) }
            return
        }

        if (current.mode == AuthMode.SignUp && current.password.length < MIN_SIGN_UP_PASSWORD_LENGTH) {
            _state.setState { it.copy(error = UiText.Resource(Res.string.auth_error_password_min_length)) }
            return
        }

        _state.setState { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                when (_state.value.mode) {
                    AuthMode.SignIn -> signIn(_state.value.email.trim(), _state.value.password)
                    AuthMode.SignUp -> signUp(
                        email = _state.value.email.trim(),
                        password = _state.value.password,
                        firstName = _state.value.firstName.trim(),
                        lastName = _state.value.lastName.trim(),
                    )
                }
                _state.setState { it.copy(password = "", isLoading = false, error = null) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _state.setState {
                    it.copy(
                        isLoading = false,
                        error = mapAuthError(exception),
                    )
                }
            }
        }
    }

}
