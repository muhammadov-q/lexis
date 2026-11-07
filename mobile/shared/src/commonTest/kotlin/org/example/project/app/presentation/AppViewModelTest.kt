package org.example.project.app.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.feature.auth.domain.model.AuthApiException
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.repository.AuthRepository
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.usecase.GetCurrentUserUseCase
import org.example.project.feature.auth.domain.usecase.SignOutUseCase
import org.example.project.feature.auth.presentation.AuthMode
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.auth_error_network
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @Test
    fun restoreSessionUpdatesAppAuthenticationState() = runAppTest {
        val expectedUser = User(7, "user@example.com", "John", "Doe")
        val sessionStore = AuthSessionStore()
        val repository = FakeAuthRepository(currentUser = expectedUser, sessionStore = sessionStore)
        val viewModel = createViewModel(repository, sessionStore)

        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAuthenticated)
        assertFalse(viewModel.state.value.isSessionChecking)
        assertEquals(AppEffect.NavigateToDashboard, viewModel.effects.first())
    }

    @Test
    fun signInRequestedEmitsAuthNavigationWithRequestedMode() = runAppTest {
        val sessionStore = AuthSessionStore()
        val viewModel = createViewModel(FakeAuthRepository(null, sessionStore), sessionStore)

        viewModel.onEvent(AppEvent.SignInRequested(AuthMode.SignUp))

        assertEquals(AppEffect.NavigateToAuth(AuthMode.SignUp), viewModel.effects.first())
    }

    @Test
    fun authenticatedSessionChangeEmitsDashboardEffect() = runAppTest {
        val sessionStore = AuthSessionStore()
        val viewModel = createViewModel(FakeAuthRepository(null, sessionStore), sessionStore)
        advanceUntilIdle()

        sessionStore.setAuthenticated(User(5, "user@example.com", null, null))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAuthenticated)
        assertEquals(AppEffect.NavigateToDashboard, viewModel.effects.first())
    }

    @Test
    fun signOutFailureStillReturnsToGuestAndSurfacesError() = runAppTest {
        val sessionStore = AuthSessionStore()
        sessionStore.setAuthenticated(User(4, "signed@in.com", null, null))
        val repository = FakeAuthRepository(
            currentUser = User(4, "signed@in.com", null, null),
            sessionStore = sessionStore,
            signOutError = AuthApiException(0),
        )
        val viewModel = createViewModel(repository, sessionStore)
        advanceUntilIdle()

        viewModel.onEvent(AppEvent.SignOutRequested)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isAuthenticated)
        assertFalse(viewModel.state.value.isSigningOut)
        assertEquals(UiText.Resource(Res.string.auth_error_network), viewModel.state.value.error)
        assertEquals(1, repository.signOutRequests)
    }

    private fun TestScope.createViewModel(
        repository: FakeAuthRepository,
        sessionStore: AuthSessionStore,
    ) = AppViewModel(
        getCurrentUser = GetCurrentUserUseCase(repository),
        signOut = SignOutUseCase(repository),
        authSessionStore = sessionStore,
    )

    private fun runAppTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class FakeAuthRepository(
    private val currentUser: User?,
    private val sessionStore: AuthSessionStore,
    private val signOutError: Throwable? = null,
) : AuthRepository {
    var signOutRequests: Int = 0

    override suspend fun signUp(email: String, password: String, firstName: String, lastName: String) =
        User(2, email, firstName, lastName)

    override suspend fun signIn(email: String, password: String) = User(1, email, null, null)

    override suspend fun signOut() {
        signOutRequests += 1
        signOutError?.let { throw it }
        sessionStore.setGuest()
    }

    override suspend fun currentUserOrNull(): User? {
        currentUser?.let(sessionStore::setAuthenticated) ?: sessionStore.setGuest()
        return currentUser
    }
}
