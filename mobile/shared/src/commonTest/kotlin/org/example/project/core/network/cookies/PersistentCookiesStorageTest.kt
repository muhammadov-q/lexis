package org.example.project.core.network.cookies

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersistentCookiesStorageTest {
    @Test
    fun cookiePathRequiresAPathSegmentBoundary() {
        assertTrue(cookiePathMatches("/auth", "/auth"))
        assertTrue(cookiePathMatches("/auth", "/auth/sign-in"))
        assertFalse(cookiePathMatches("/auth", "/authentication"))
    }
}
