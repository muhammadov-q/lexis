package org.example.project.core.persistence

expect object AuthCookiePersistence : CookiePersistence {
    override fun read(): String?
    override fun write(value: String)
    override fun clear()
}

