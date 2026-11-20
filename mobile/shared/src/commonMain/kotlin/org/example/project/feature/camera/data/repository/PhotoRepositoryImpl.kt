package org.example.project.feature.camera.data.repository

import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.camera.data.network.PhotoUploadApi
import org.example.project.feature.camera.data.network.PhotoUploadConfig
import org.example.project.feature.camera.data.network.models.mapTo
import org.example.project.feature.camera.domain.model.ApiException
import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.OcrModel
import org.example.project.feature.camera.domain.model.OcrModels
import org.example.project.feature.camera.domain.repository.PhotoRepository

class PhotoRepositoryImpl(
    private val api: PhotoUploadApi,
    private val config: PhotoUploadConfig,
    private val authSessionStore: AuthSessionStore,
) : PhotoRepository {
    override suspend fun getModels(): OcrModels {
        val response = api.getModels()
        return OcrModels(response.models.map { OcrModel(it.id, it.name, it.description) }, response.defaultModel)
    }

    override suspend fun uploadPhoto(photo: ByteArray, model: String?): Prediction {
        val isAuthenticatedRequest = authSessionStore.isAuthenticated()
        val uploadPath = if (isAuthenticatedRequest) {
            config.authenticatedUploadPath
        } else {
            config.guestUploadPath
        }

        val response = try {
            api.upload(photo, uploadPath, model)
        } catch (exception: ApiException) {
            if (isAuthenticatedRequest && exception.status == 401) {
                api.upload(photo, config.guestUploadPath, model)
            } else {
                throw exception
            }
        }

        return mapTo(response)
    }
}
