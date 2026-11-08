package org.example.project.feature.camera.di

import org.example.project.core.network.platform.localBackendBaseUrl
import org.example.project.feature.camera.data.network.PhotoUploadApi
import org.example.project.feature.camera.data.network.PhotoUploadConfig
import org.example.project.feature.camera.data.repository.PhotoRepositoryImpl
import org.example.project.feature.camera.domain.repository.PhotoRepository
import org.example.project.feature.camera.domain.usecase.UploadPhotoUseCase
import org.example.project.feature.camera.presentation.CameraViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val cameraModule = module {
    single {
        PhotoUploadConfig(
            baseUrl = localBackendBaseUrl(),
            guestUploadPath = "/predict",
            authenticatedUploadPath = "/recognitions",
            multipartFieldName = "image",
        )
    }
    single { PhotoUploadApi(get(), get()) }
    single<PhotoRepository> {
        PhotoRepositoryImpl(
            api = get(),
            config = get(),
            authSessionStore = get(),
        )
    }
    factory { UploadPhotoUseCase(get()) }
    viewModelOf(::CameraViewModel)
}
