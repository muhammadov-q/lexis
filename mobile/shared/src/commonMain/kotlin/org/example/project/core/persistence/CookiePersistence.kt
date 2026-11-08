package org.example.project.core.persistence

interface CookiePersistence {
    fun read(): String?
    fun write(value: String)
    fun clear()
}
