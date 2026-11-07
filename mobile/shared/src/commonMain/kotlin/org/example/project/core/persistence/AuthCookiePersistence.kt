package org.example.project.core.persistence

expect object AuthCookiePersistence {
    fun read(): String?
    fun write(value: String)
    fun clear()
}


