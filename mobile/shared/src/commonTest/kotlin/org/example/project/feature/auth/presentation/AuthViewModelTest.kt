package org.example.project.feature.auth.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.example.project.feature.auth.domain.usecase.SignInUseCase
import org.example.project.feature.auth.domain.usecase.SignUpUseCase
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.auth_error_password_min_length
import scriptlens.shared.generated.resources.auth_error_validation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @Test
    fun toggleModeSwitchesBetweenSignInAndSignUp() = runAuthTest {
        val viewModel = createViewModel(FakeAuthRepository())

        assertEquals(AuthMode.SignIn, viewModel.state.value.mode)
        viewModel.onEvent(AuthEvent.ToggleModeRequested)
        assertEquals(AuthMode.SignUp, viewModel.state.value.mode)
        viewModel.onEvent(AuthEvent.ToggleModeRequested)
        assertEquals(AuthMode.SignIn, viewModel.state.value.mode)
    }

    @Test
    fun initializeModeOnlyAppliesOnce() = runAuthTest {
        val viewModel = createViewModel(FakeAuthRepository())

        viewModel.initializeMode(AuthMode.SignUp)
        viewModel.onEvent(AuthEvent.ToggleModeRequested)
        viewModel.initializeMode(AuthMode.SignUp)

        assertEquals(AuthMode.SignIn, viewModel.state.value.mode)
    }

    @Test
    fun submitSignInCallsUseCaseAndClearsPassword() = runAuthTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.onEvent(AuthEvent.EmailChanged(" user@example.com "))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.SubmitRequested)
        advanceUntilIdle()

        assertEquals("user@example.com", repository.lastSignInEmail)
        assertEquals("", viewModel.state.value.password)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun submitWithBlankCredentialsShowsValidationErrorAndSkipsRequests() = runAuthTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.onEvent(AuthEvent.SubmitRequested)

        assertEquals(UiText.Resource(Res.string.auth_error_validation), viewModel.state.value.error)
        assertEquals(0, repository.signInRequests)
        assertEquals(0, repository.signUpRequests)
    }

    @Test
    fun signUpWithShortPasswordShowsValidationError() = runAuthTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.initializeMode(AuthMode.SignUp)
        viewModel.onEvent(AuthEvent.EmailChanged("new@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("short"))
        viewModel.onEvent(AuthEvent.SubmitRequested)

        assertEquals(UiText.Resource(Res.string.auth_error_password_min_length), viewModel.state.value.error)
        assertEquals(0, repository.signUpRequests)
    }

    @Test
    fun duplicateSubmitWhileLoadingTriggersSingleRequest() = runAuthTest {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.onEvent(AuthEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("password123"))
        viewModel.onEvent(AuthEvent.SubmitRequested)
        viewModel.onEvent(AuthEvent.SubmitRequested)
        advanceUntilIdle()

        assertEquals(1, repository.signInRequests)
    }

    private fun TestScope.createViewModel(repository: FakeAuthRepository): AuthViewModel = AuthViewModel(
        signIn = SignInUseCase(repository),
        signUp = SignUpUseCase(repository),
    )

    private fun runAuthTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class FakeAuthRepository : AuthRepository {
    var signInRequests: Int = 0
    var signUpRequests: Int = 0
    var lastSignInEmail: String? = null

    override suspend fun signUp(email: String, password: String, firstName: String, lastName: String): User {
        signUpRequests += 1
        return User(2, email, firstName, lastName)
    }

    override suspend fun signIn(email: String, password: String): User {
        signInRequests += 1
        lastSignInEmail = email
        return User(1, email, "First", "Last")
    }

    override suspend fun signOut() = Unit

    override suspend fun currentUserOrNull(): User? = null
}
