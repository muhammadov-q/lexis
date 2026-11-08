package org.example.project.feature.camera.data.repository

import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.camera.data.network.PhotoUploadApi
import org.example.project.feature.camera.data.network.PhotoUploadConfig
import org.example.project.feature.camera.data.network.models.PredictionDto
import org.example.project.feature.camera.domain.model.ApiException
import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.PredictionBox
import org.example.project.feature.camera.domain.model.PredictionGloss
import org.example.project.feature.camera.domain.model.WordPrediction
import org.example.project.feature.camera.domain.repository.PhotoRepository

class PhotoRepositoryImpl(
    private val api: PhotoUploadApi,
    private val config: PhotoUploadConfig,
    private val authSessionStore: AuthSessionStore,
) : PhotoRepository {
    override suspend fun uploadPhoto(photo: ByteArray): Prediction {
        val isAuthenticatedRequest = authSessionStore.isAuthenticated()
        val uploadPath = if (isAuthenticatedRequest) {
            config.authenticatedUploadPath
        } else {
            config.guestUploadPath
        }

        val response = try {
            api.upload(photo, uploadPath)
        } catch (exception: ApiException) {
            if (isAuthenticatedRequest && exception.status == 401) {
                api.upload(photo, config.guestUploadPath)
            } else {
                throw exception
            }
        }

        return response.toDomain()
    }

    private fun PredictionDto.toDomain(): Prediction {
        return Prediction(
            predictions = predictions.map { prediction ->
                WordPrediction(
                    text = prediction.text,
                    probabilities = prediction.probabilities,
                    box = PredictionBox(
                        x = prediction.box.x,
                        y = prediction.box.y,
                        w = prediction.box.w,
                        h = prediction.box.h,
                    ),
                    line = prediction.line,
                    gloss = PredictionGloss(
                        lemma = prediction.gloss?.lemma.orEmpty(),
                        translation = prediction.gloss?.translation.orEmpty(),
                        definition = prediction.gloss?.definition.orEmpty(),
                    ),
                )
            },
            modelVersion = modelVersion,
        )
    }
}
