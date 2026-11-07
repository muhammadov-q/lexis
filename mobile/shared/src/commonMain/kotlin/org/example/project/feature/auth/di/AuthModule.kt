package org.example.project.feature.auth.di

import org.example.project.core.network.platform.localBackendBaseUrl
import org.example.project.feature.auth.data.network.AuthApi
import org.example.project.feature.auth.data.network.AuthConfig
import org.example.project.feature.auth.data.repository.AuthRepositoryImpl
import org.example.project.feature.auth.domain.repository.AuthRepository
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.auth.domain.usecase.GetCurrentUserUseCase
import org.example.project.feature.auth.domain.usecase.SignInUseCase
import org.example.project.feature.auth.domain.usecase.SignOutUseCase
import org.example.project.feature.auth.domain.usecase.SignUpUseCase
import org.example.project.feature.auth.presentation.AuthViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    single { AuthSessionStore() }
    single { AuthConfig(baseUrl = localBackendBaseUrl()) }
    single { AuthApi(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(api = get(), sessionCookieStore = get(), sessionStore = get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { SignInUseCase(get()) }
    factory { SignUpUseCase(get()) }
    factory { SignOutUseCase(get()) }
    viewModelOf(::AuthViewModel)
}
