package org.example.project.core.network.di

import org.example.project.core.network.cookies.PersistentCookiesStorage
import org.example.project.core.persistence.SessionCookieStore
import org.example.project.core.network.platform.createHttpClient
import org.koin.dsl.module

val coreNetworkModule = module {
    single { PersistentCookiesStorage() }
    single<SessionCookieStore> { get<PersistentCookiesStorage>() }
    single { createHttpClient(get<PersistentCookiesStorage>()) }
}
