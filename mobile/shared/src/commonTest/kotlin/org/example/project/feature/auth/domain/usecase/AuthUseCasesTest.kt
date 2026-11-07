package org.example.project.feature.auth.domain.usecase

import kotlinx.coroutines.test.runTest
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.repository.AuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthUseCasesTest {
    @Test
    fun signInUseCaseDelegatesToRepository() = runTest {
        val user = User(1, "user@example.com", "John", "Doe")
        val repository = FakeAuthRepository(signInResult = user)
        val useCase = SignInUseCase(repository)

        val result = useCase("user@example.com", "password123")

        assertEquals(user, result)
        assertEquals("user@example.com", repository.lastSignInEmail)
    }

    @Test
    fun signUpUseCaseDelegatesToRepository() = runTest {
        val user = User(2, "new@example.com", "Jane", "Doe")
        val repository = FakeAuthRepository(signUpResult = user)
        val useCase = SignUpUseCase(repository)

        val result = useCase("new@example.com", "password123", "Jane", "Doe")

        assertEquals(user, result)
        assertEquals("new@example.com", repository.lastSignUpEmail)
    }

    @Test
    fun getCurrentUserUseCaseReturnsNullableUser() = runTest {
        val repository = FakeAuthRepository(currentUser = null)
        val useCase = GetCurrentUserUseCase(repository)

        val result = useCase()

        assertNull(result)
    }

    @Test
    fun signOutUseCaseDelegatesToRepository() = runTest {
        val repository = FakeAuthRepository()
        val useCase = SignOutUseCase(repository)

        useCase()

        assertEquals(1, repository.signOutCount)
    }
}

private class FakeAuthRepository(
    private val signInResult: User = User(1, "user@example.com", "John", "Doe"),
    private val signUpResult: User = User(2, "new@example.com", "Jane", "Doe"),
    private val currentUser: User? = User(1, "user@example.com", "John", "Doe"),
) : AuthRepository {
    var lastSignInEmail: String? = null
    var lastSignUpEmail: String? = null
    var signOutCount: Int = 0

    override suspend fun signUp(email: String, password: String, firstName: String, lastName: String): User {
        lastSignUpEmail = email
        return signUpResult
    }

    override suspend fun signIn(email: String, password: String): User {
        lastSignInEmail = email
        return signInResult
    }

    override suspend fun signOut() {
        signOutCount += 1
    }

    override suspend fun currentUserOrNull(): User? = currentUser
}

