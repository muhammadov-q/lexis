package org.example.project.feature.camera.data.repository

import org.example.project.core.persistence.SessionCookieStore
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
import org.example.project.feature.history.domain.session.HistoryRefreshStore

class PhotoRepositoryImpl(
    private val api: PhotoUploadApi,
    private val config: PhotoUploadConfig,
    private val authSessionStore: AuthSessionStore,
    private val sessionCookieStore: SessionCookieStore,
    private val historyRefreshStore: HistoryRefreshStore,
) : PhotoRepository {
    override suspend fun uploadPhoto(photo: ByteArray): Prediction {
        val isAuthenticatedRequest = authSessionStore.isAuthenticated()
        val uploadPath = if (isAuthenticatedRequest) {
            config.authenticatedUploadPath
        } else {
            config.guestUploadPath
        }

        var savedToHistory = false
        val response = try {
            if (isAuthenticatedRequest && uploadPath == config.authenticatedUploadPath) {
                savedToHistory = true
            }
            api.upload(photo, uploadPath)
        } catch (exception: ApiException) {
            if (isAuthenticatedRequest && exception.status == 401) {
                // Session expired while trying to save a recognition: continue as guest.
                sessionCookieStore.clearSessionCookie()
                authSessionStore.setGuest()
                savedToHistory = false
                api.upload(photo, config.guestUploadPath)
            } else {
                throw exception
            }
        }

        if (savedToHistory) {
            historyRefreshStore.markHistoryDirty()
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
