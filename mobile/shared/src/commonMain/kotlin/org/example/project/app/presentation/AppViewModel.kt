package org.example.project.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.session.SessionMode
import org.example.project.feature.auth.domain.usecase.GetCurrentUserUseCase
import org.example.project.feature.auth.domain.usecase.SignOutUseCase
import org.example.project.feature.auth.presentation.mapAuthError

class AppViewModel(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val signOut: SignOutUseCase,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow(AppContract())
    val state = _state.asStateFlow()

    private val effectChannel = Channel<AppEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var previousSessionMode: SessionMode? = null
    private var navigateAfterSignIn = false

    init {
        observeSession()
        restoreSession()
    }

    fun onEvent(event: AppEvent) {
        when (event) {
            is AppEvent.SignInRequested -> {
                navigateAfterSignIn = true
                sendEffect(AppEffect.NavigateToAuth(event.mode))
            }
            is AppEvent.SignOutRequested -> requestSignOut()
            is AppEvent.GuestContinueRequested -> {
                navigateAfterSignIn = false
                sendEffect(AppEffect.NavigateToDashboard)
            }
            is AppEvent.DismissErrorRequested -> _state.update { it.copy(error = null) }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authSessionStore.mode.collect { mode ->
                _state.update { it.copy(isAuthenticated = mode is SessionMode.Authenticated) }
                if (
                    navigateAfterSignIn &&
                    mode is SessionMode.Authenticated &&
                    previousSessionMode !is SessionMode.Authenticated
                ) {
                    navigateAfterSignIn = false
                    sendEffect(AppEffect.NavigateToDashboard)
                }
                previousSessionMode = mode
            }
        }
    }

    private fun restoreSession() {
        viewModelScope.launch {
            try {
                getCurrentUser()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _state.update { it.copy(error = mapAuthError(exception)) }
            } finally {
                _state.update {
                    it.copy(
                        isAuthenticated = authSessionStore.isAuthenticated(),
                        isSessionChecking = false,
                    )
                }
            }
        }
    }

    private fun requestSignOut() {
        if (_state.value.isSigningOut) return

        _state.update {
            it.copy(
                isAuthenticated = false,
                isSigningOut = true,
                error = null,
            )
        }
        navigateAfterSignIn = false
        authSessionStore.setGuest()
        sendEffect(AppEffect.NavigateToCameraRoot)
        viewModelScope.launch {
            try {
                signOut()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _state.update { it.copy(error = mapAuthError(exception)) }
            } finally {
                authSessionStore.setGuest()
                _state.update {
                    it.copy(
                        isAuthenticated = false,
                        isSigningOut = false,
                    )
                }
            }
        }
    }

    private fun sendEffect(effect: AppEffect) {
        check(effectChannel.trySend(effect).isSuccess) {
            "Unable to deliver app effect: $effect"
        }
    }
}
