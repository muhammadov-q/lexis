package org.example.project.core.network.cookies

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.core.persistence.AuthCookiePersistence
import org.example.project.core.persistence.SessionCookieStore

private const val SESSION_COOKIE_NAME = "team4_session"

/**
 * Persists auth cookies so mobile sessions survive app restarts.
 */
class PersistentCookiesStorage : CookiesStorage, SessionCookieStore {
    private val lock = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    private val cookies = loadCookies().toMutableList()

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        lock.withLock {
            val normalized = PersistedCookie.of(requestUrl, cookie)
            cookies.removeAll { it.name == normalized.name && it.originHost == normalized.originHost }

            if (normalized.isExpired() || normalized.value.isBlank()) {
                persistCookies()
                return
            }

            cookies += normalized
            persistCookies()
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> =
        lock.withLock {
            pruneExpiredLocked()
            cookies
                .asSequence()
                .filter { it.matches(requestUrl) }
                .map { it.toCookie() }
                .toList()
        }

    override fun close() {
        // No resources to close.
    }

    override fun clearSessionCookie() {
        val filtered = cookies.filterNot { it.name == SESSION_COOKIE_NAME }
        cookies.clear()
        cookies.addAll(filtered)
        persistCookies()
    }

    private fun loadCookies(): List<PersistedCookie> {
        val raw = AuthCookiePersistence.read().orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching {
            json.decodeFromString<List<PersistedCookie>>(raw)
        }.getOrElse {
            emptyList()
        }.filterNot { it.isExpired() }
    }

    private fun pruneExpiredLocked() {
        val active = cookies.filterNot { it.isExpired() }
        if (active.size != cookies.size) {
            cookies.clear()
            cookies.addAll(active)
            persistCookies()
        }
    }

    private fun persistCookies() {
        if (cookies.isEmpty()) {
            AuthCookiePersistence.clear()
            return
        }
        AuthCookiePersistence.write(json.encodeToString(cookies))
    }
}

@Serializable
private data class PersistedCookie(
    val name: String,
    val value: String,
    val originHost: String,
    val path: String,
    val domain: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
    val expiresAtEpochMillis: Long? = null,
) {
    fun isExpired(nowEpochMillis: Long = GMTDate().timestamp): Boolean =
        expiresAtEpochMillis?.let { it <= nowEpochMillis } ?: false

    fun matches(requestUrl: Url): Boolean {
        if (secure && requestUrl.protocol.name != "https") return false

        val host = requestUrl.host.lowercase()
        val cookieDomain = domain?.trim()?.removePrefix(".")?.lowercase()
        val expectedHost = cookieDomain ?: originHost.lowercase()
        val hostMatches = host == expectedHost || host.endsWith(".$expectedHost")
        if (!hostMatches) return false

        val requestPath = requestUrl.encodedPath.ifBlank { "/" }
        return cookiePathMatches(path, requestPath)
    }

    fun toCookie(): Cookie = Cookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        secure = secure,
        httpOnly = httpOnly,
        expires = expiresAtEpochMillis?.let(::GMTDate),
    )

    companion object {
        fun of(requestUrl: Url, cookie: Cookie): PersistedCookie {
            val cookiePath = cookie.path?.takeIf { it.startsWith("/") } ?: "/"
            val expiresAt = cookie.expires?.timestamp
                ?: cookie.maxAge?.takeIf { it >= 0 }?.let { GMTDate().timestamp + it * 1000L }

            return PersistedCookie(
                name = cookie.name,
                value = cookie.value,
                originHost = requestUrl.host,
                path = cookiePath,
                domain = cookie.domain,
                secure = cookie.secure,
                httpOnly = cookie.httpOnly,
                expiresAtEpochMillis = expiresAt,
            )
        }
    }

}

internal fun cookiePathMatches(cookiePath: String, requestPath: String): Boolean =
    requestPath == cookiePath ||
        (requestPath.startsWith(cookiePath) &&
            (cookiePath.endsWith("/") || requestPath.getOrNull(cookiePath.length) == '/'))
