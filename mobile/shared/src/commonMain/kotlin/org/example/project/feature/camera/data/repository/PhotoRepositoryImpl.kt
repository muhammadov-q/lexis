package org.example.project.feature.camera.data.repository

import org.example.project.feature.camera.data.network.PhotoUploadApi
import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.PredictionBox
import org.example.project.feature.camera.domain.model.PredictionGloss
import org.example.project.feature.camera.domain.model.WordPrediction
import org.example.project.feature.camera.domain.repository.PhotoRepository

class PhotoRepositoryImpl(
    private val api: PhotoUploadApi,
) : PhotoRepository {
    override suspend fun uploadPhoto(photo: ByteArray): Prediction {
        val response = api.upload(photo)
        return Prediction(
            predictions = response.predictions.map { prediction ->
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
            modelVersion = response.modelVersion,
        )
    }
}

