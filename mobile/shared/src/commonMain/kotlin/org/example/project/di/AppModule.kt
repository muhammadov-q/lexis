package org.example.project.di

import org.example.project.app.presentation.AppViewModel
import org.example.project.core.network.di.coreNetworkModule
import org.example.project.feature.auth.di.authModule
import org.example.project.feature.camera.di.cameraModule
import org.example.project.feature.history.di.historyModule
import org.example.project.feature.historydetail.di.historyDetailModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    includes(coreNetworkModule, authModule, cameraModule, historyModule, historyDetailModule)
    viewModelOf(::AppViewModel)
}
