package org.example.project.feature.historydetail.di

import org.example.project.feature.historydetail.presentation.HistoryDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val historyDetailModule = module {
    viewModelOf(::HistoryDetailViewModel)
}
