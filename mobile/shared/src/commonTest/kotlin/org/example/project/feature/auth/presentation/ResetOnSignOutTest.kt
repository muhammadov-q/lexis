package org.example.project.feature.auth.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.session.AuthSessionStore
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ResetOnSignOutTest {
    private class CountingViewModel(sessionStore: AuthSessionStore) : ViewModel() {
        var resets = 0

        init {
            resetOnSignOut(sessionStore) { resets++ }
        }
    }

    @Test
    fun aGuestFromTheStartIsNotReset() = runResetTest {
        val viewModel = CountingViewModel(AuthSessionStore())
        advanceUntilIdle()

        assertEquals(0, viewModel.resets)
    }

    @Test
    fun signingOutResetsOnce() = runResetTest {
        val session = signedIn()
        val viewModel = CountingViewModel(session)
        advanceUntilIdle()

        session.setGuest()
        advanceUntilIdle()
        session.setGuest()
        advanceUntilIdle()

        assertEquals(1, viewModel.resets)
    }

    @Test
    fun signingOutBeforeTheViewModelListensStillResets() = runResetTest {
        val session = signedIn()
        val viewModel = CountingViewModel(session)

        session.setGuest()
        advanceUntilIdle()

        assertEquals(1, viewModel.resets)
    }

    @Test
    fun signingInThenOutAsAGuestResets() = runResetTest {
        val session = AuthSessionStore()
        val viewModel = CountingViewModel(session)
        advanceUntilIdle()

        session.setAuthenticated(USER)
        advanceUntilIdle()
        session.setGuest()
        advanceUntilIdle()

        assertEquals(1, viewModel.resets)
    }

    private fun signedIn() = AuthSessionStore().apply { setAuthenticated(USER) }

    private fun runResetTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private companion object {
        val USER = User(id = 1, email = "reader@example.com", firstName = null, lastName = null)
    }
}
