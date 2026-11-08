package org.example.project.core.network.di

import org.example.project.core.network.cookies.PersistentCookiesStorage
import org.example.project.core.persistence.AuthCookiePersistence
import org.example.project.core.persistence.CookiePersistence
import org.example.project.core.persistence.SessionCookieStore
import org.example.project.core.network.platform.createHttpClient
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.koin.dsl.module

val coreNetworkModule = module {
    single<CookiePersistence> { AuthCookiePersistence }
    single { PersistentCookiesStorage(get()) }
    single<SessionCookieStore> { get<PersistentCookiesStorage>() }
    single { createHttpClient(get<PersistentCookiesStorage>(), get<AuthSessionStore>()) }
}
