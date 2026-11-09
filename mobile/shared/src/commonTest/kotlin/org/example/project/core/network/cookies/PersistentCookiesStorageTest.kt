package org.example.project.core.network.cookies

import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.test.runTest
import org.example.project.core.persistence.CookiePersistence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PersistentCookiesStorageTest {
    private val requestUrl = Url("https://example.com/")

    @Test
    fun cookiePathRequiresAPathSegmentBoundary() {
        assertTrue(cookiePathMatches("/auth", "/auth"))
        assertTrue(cookiePathMatches("/auth", "/auth/sign-in"))
        assertFalse(cookiePathMatches("/auth", "/authentication"))
    }

    @Test
    fun addingCookieReplacesExistingCookieWithSameNameAndOrigin() = runTest {
        val persistence = InMemoryCookiePersistence()
        val storage = PersistentCookiesStorage(persistence)

        storage.addCookie(requestUrl, Cookie(name = "lexis_session", value = "old"))
        storage.addCookie(requestUrl, Cookie(name = "lexis_session", value = "new"))

        assertEquals(listOf("new"), storage.get(requestUrl).map { it.value })
    }

    @Test
    fun expiredCookieIsNotStored() = runTest {
        val persistence = InMemoryCookiePersistence()
        val storage = PersistentCookiesStorage(persistence)

        storage.addCookie(
            requestUrl,
            Cookie(name = "lexis_session", value = "expired", expires = GMTDate(0L)),
        )

        assertTrue(storage.get(requestUrl).isEmpty())
        assertNull(persistence.value)
    }

    @Test
    fun clearingSessionCookieKeepsOtherCookies() = runTest {
        val persistence = InMemoryCookiePersistence()
        val storage = PersistentCookiesStorage(persistence)
        storage.addCookie(requestUrl, Cookie(name = "lexis_session", value = "session"))
        storage.addCookie(requestUrl, Cookie(name = "preference", value = "dark"))

        storage.clearSessionCookie()

        assertEquals(listOf("preference"), storage.get(requestUrl).map { it.name })
    }

    @Test
    fun cookiesReloadFromPersistenceAfterStorageIsRecreated() = runTest {
        val persistence = InMemoryCookiePersistence()
        PersistentCookiesStorage(persistence)
            .addCookie(requestUrl, Cookie(name = "lexis_session", value = "saved"))

        val restartedStorage = PersistentCookiesStorage(persistence)

        assertEquals(listOf("saved"), restartedStorage.get(requestUrl).map { it.value })
    }

    private class InMemoryCookiePersistence : CookiePersistence {
        var value: String? = null
            private set

        override fun read(): String? = value

        override fun write(value: String) {
            this.value = value
        }

        override fun clear() {
            value = null
        }
    }
}
