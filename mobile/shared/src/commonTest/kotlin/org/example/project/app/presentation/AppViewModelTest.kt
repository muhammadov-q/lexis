package org.example.project.app.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.example.project.feature.auth.domain.usecase.SignInUseCase
import org.example.project.feature.auth.domain.usecase.SignOutUseCase
import org.example.project.feature.auth.domain.usecase.SignUpUseCase
import org.example.project.feature.auth.presentation.AuthEvent
import org.example.project.feature.auth.presentation.AuthMode
import org.example.project.feature.auth.presentation.AuthViewModel
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.auth_error_network
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @Test
    fun restoreSessionUpdatesAppAuthenticationState() = runAppTest {
        val expectedUser = User(7, "user@example.com", "John", "Doe")
        val sessionStore = AuthSessionStore()
        val repository = FakeAuthRepository(currentUser = expectedUser, sessionStore = sessionStore)
        val viewModel = createViewModel(repository, sessionStore)
        var navigationEffect: AppEffect? = null
        val effectCollector = launch { navigationEffect = viewModel.effects.first() }

        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAuthenticated)
        assertFalse(viewModel.state.value.isSessionChecking)
        assertNull(navigationEffect)
        effectCollector.cancel()
    }

    @Test
    fun startingAsGuestFinishesSessionRestorationWithoutAuthentication() = runAppTest {
        val sessionStore = AuthSessionStore()
        val viewModel = createViewModel(FakeAuthRepository(null, sessionStore), sessionStore)

        assertTrue(viewModel.state.value.isSessionChecking)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isAuthenticated)
        assertFalse(viewModel.state.value.isSessionChecking)
    }

    @Test
    fun signInRequestedEmitsAuthNavigationWithRequestedMode() = runAppTest {
        val sessionStore = AuthSessionStore()
        val viewModel = createViewModel(FakeAuthRepository(null, sessionStore), sessionStore)

        viewModel.onEvent(AppEvent.SignInRequested(AuthMode.SignUp))

        assertEquals(AppEffect.NavigateToAuth(AuthMode.SignUp), viewModel.effects.first())
    }

    @Test
    fun successfulSignInEmitsDashboardEffect() = runAppTest {
        assertSuccessfulAuthentication(AuthMode.SignIn)
    }

    @Test
    fun successfulRegistrationEmitsDashboardEffect() = runAppTest {
        assertSuccessfulAuthentication(AuthMode.SignUp)
    }

    private suspend fun TestScope.assertSuccessfulAuthentication(mode: AuthMode) {
        val sessionStore = AuthSessionStore()
        val repository = FakeAuthRepository(null, sessionStore)
        val viewModel = createViewModel(repository, sessionStore)
        val authViewModel = createAuthViewModel(repository)
        val effects = mutableListOf<AppEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects.add(it) } }
        advanceUntilIdle()

        viewModel.onEvent(AppEvent.SignInRequested(mode))
        authViewModel.initializeMode(mode)
        authViewModel.onEvent(AuthEvent.EmailChanged("user@example.com"))
        authViewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        authViewModel.onEvent(AuthEvent.SubmitRequested)
        advanceUntilIdle()

        assertTrue(sessionStore.isAuthenticated())
        assertTrue(viewModel.state.value.isAuthenticated)
        assertNull(authViewModel.state.value.error)
        assertEquals(listOf(AppEffect.NavigateToAuth(mode), AppEffect.NavigateToDashboard), effects)
    }

    @Test
    fun failedAuthenticationStaysGuestWithoutDashboardNavigation() = runAppTest {
        for (mode in AuthMode.entries) {
            val sessionStore = AuthSessionStore()
            val repository = FakeAuthRepository(null, sessionStore, authError = AuthApiException(0))
            val viewModel = createViewModel(repository, sessionStore)
            val authViewModel = createAuthViewModel(repository)
            val effects = mutableListOf<AppEffect>()
            val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.effects.collect { effects.add(it) }
            }
            advanceUntilIdle()

            viewModel.onEvent(AppEvent.SignInRequested(mode))
            authViewModel.initializeMode(mode)
            authViewModel.onEvent(AuthEvent.EmailChanged("user@example.com"))
            authViewModel.onEvent(AuthEvent.PasswordChanged("password123"))
            authViewModel.onEvent(AuthEvent.SubmitRequested)
            advanceUntilIdle()

            assertFalse(sessionStore.isAuthenticated())
            assertFalse(viewModel.state.value.isAuthenticated)
            assertEquals(UiText.Resource(Res.string.auth_error_network), authViewModel.state.value.error)
            assertEquals(listOf<AppEffect>(AppEffect.NavigateToAuth(mode)), effects)
            collector.cancel()
        }
    }

    @Test
    fun guestContinuationClearsPendingAuthenticationNavigation() = runAppTest {
        val sessionStore = AuthSessionStore()
        val viewModel = createViewModel(FakeAuthRepository(null, sessionStore), sessionStore)
        val effects = mutableListOf<AppEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects.add(it) } }
        advanceUntilIdle()

        viewModel.onEvent(AppEvent.SignInRequested(AuthMode.SignIn))
        viewModel.onEvent(AppEvent.GuestContinueRequested)
        advanceUntilIdle()

        assertFalse(sessionStore.isAuthenticated())
        assertFalse(viewModel.state.value.isAuthenticated)
        assertEquals(listOf(AppEffect.NavigateToAuth(AuthMode.SignIn), AppEffect.NavigateToDashboard), effects)

        sessionStore.setAuthenticated(User(5, "user@example.com", null, null))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isAuthenticated)
        assertEquals(listOf(AppEffect.NavigateToAuth(AuthMode.SignIn), AppEffect.NavigateToDashboard), effects)
    }

    @Test
    fun signOutImmediatelyReturnsToGuestAndClearsPendingNavigation() = runAppTest {
        val user = User(4, "signed@in.com", null, null)
        val sessionStore = AuthSessionStore()
        val repository = FakeAuthRepository(user, sessionStore)
        val viewModel = createViewModel(repository, sessionStore)
        val effects = mutableListOf<AppEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects.add(it) } }
        advanceUntilIdle()

        viewModel.onEvent(AppEvent.SignInRequested(AuthMode.SignIn))
        viewModel.onEvent(AppEvent.SignOutRequested)

        assertFalse(sessionStore.isAuthenticated())
        assertFalse(viewModel.state.value.isAuthenticated)
        assertTrue(viewModel.state.value.isSigningOut)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSigningOut)
        assertEquals(1, repository.signOutRequests)
        assertEquals(listOf(AppEffect.NavigateToAuth(AuthMode.SignIn), AppEffect.NavigateToCameraRoot), effects)

        sessionStore.setAuthenticated(user)
        advanceUntilIdle()
        assertEquals(listOf(AppEffect.NavigateToAuth(AuthMode.SignIn), AppEffect.NavigateToCameraRoot), effects)
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
        assertEquals(AppEffect.NavigateToCameraRoot, viewModel.effects.first())
    }

    private fun TestScope.createViewModel(
        repository: FakeAuthRepository,
        sessionStore: AuthSessionStore,
    ) = AppViewModel(
        getCurrentUser = GetCurrentUserUseCase(repository),
        signOut = SignOutUseCase(repository),
        authSessionStore = sessionStore,
    )

    private fun createAuthViewModel(repository: FakeAuthRepository) = AuthViewModel(
        signIn = SignInUseCase(repository),
        signUp = SignUpUseCase(repository),
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
    private val authError: Throwable? = null,
) : AuthRepository {
    var signOutRequests: Int = 0

    override suspend fun signUp(email: String, password: String, firstName: String, lastName: String): User {
        authError?.let { throw it }
        return User(2, email, firstName, lastName).also(sessionStore::setAuthenticated)
    }

    override suspend fun signIn(email: String, password: String): User {
        authError?.let { throw it }
        return User(1, email, null, null).also(sessionStore::setAuthenticated)
    }

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
