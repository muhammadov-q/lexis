package org.example.project.feature.history.di

import org.example.project.core.network.platform.localBackendBaseUrl
import org.example.project.feature.history.data.network.HistoryApi
import org.example.project.feature.history.data.repository.HistoryRepositoryImpl
import org.example.project.feature.history.domain.repository.HistoryRepository
import org.example.project.feature.history.domain.usecase.GetRecognitionDetailUseCase
import org.example.project.feature.history.domain.usecase.GetRecognitionHistoryUseCase
import org.example.project.feature.history.presentation.HistoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val historyModule = module {
    single { HistoryApi(client = get(), baseUrl = localBackendBaseUrl()) }
    single<HistoryRepository> { HistoryRepositoryImpl(api = get()) }
    factory { GetRecognitionHistoryUseCase(get()) }
    factory { GetRecognitionDetailUseCase(get()) }
    viewModelOf(::HistoryViewModel)
}
