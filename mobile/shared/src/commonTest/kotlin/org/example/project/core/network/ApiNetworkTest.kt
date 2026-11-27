package org.example.project.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApiNetworkTest {
    @Test
    fun urlJoinsTheBaseAndThePathWithOneSlash() {
        assertEquals("http://localhost:8000/models", ApiConfig("http://localhost:8000/").url("/models"))
        assertEquals("http://localhost:8000/auth/me", ApiConfig("http://localhost:8000").url("auth/me"))
    }

    @Test
    fun apiExceptionReadsTheBackendDetail() {
        val exception = apiException(409, """{"detail":"This word is already in your vocabulary."}""")

        assertEquals(409, exception.status)
        assertEquals("This word is already in your vocabulary.", exception.detail)
    }

    @Test
    fun apiExceptionWithoutADetailKeepsTheStatus() {
        val exception = apiException(500, "Internal Server Error")

        assertEquals(500, exception.status)
        assertNull(exception.detail)
    }
}
