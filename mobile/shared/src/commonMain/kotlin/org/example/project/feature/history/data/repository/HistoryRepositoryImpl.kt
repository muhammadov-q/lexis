package org.example.project.feature.history.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CancellationException
import org.example.project.feature.history.data.network.HistoryApi
import org.example.project.feature.history.data.network.models.RecognitionDto
import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.PredictionBox
import org.example.project.feature.camera.domain.model.PredictionGloss
import org.example.project.feature.camera.domain.model.WordPrediction
import org.example.project.feature.history.domain.model.RecognitionDetail
import org.example.project.feature.history.domain.model.RecognitionHistoryItem
import org.example.project.feature.history.domain.repository.HistoryRepository

class HistoryRepositoryImpl(
    private val api: HistoryApi,
) : HistoryRepository {
    override suspend fun getHistory(): List<RecognitionHistoryItem> {
        return api.getRecognitions().map { it.toHistoryItem() }
    }

    override suspend fun getRecognitionDetail(id: Long): RecognitionDetail {
        return coroutineScope {
            val recognition = async { api.getRecognition(id) }
            val image = async {
                try {
                    api.getRecognitionImage(id)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    null
                }
            }
            recognition.await().toDetail(image.await())
        }
    }

    private fun RecognitionDto.toHistoryItem(): RecognitionHistoryItem {
        val words = predictions.take(6).joinToString(" ") { it.text }
        return RecognitionHistoryItem(
            id = id,
            createdAt = createdAt,
            filename = filename,
            modelVersion = modelVersion,
            previewText = words,
        )
    }

    private fun RecognitionDto.toDetail(imageBytes: ByteArray?): RecognitionDetail {
        return RecognitionDetail(
            id = id,
            createdAt = createdAt,
            filename = filename,
            modelVersion = modelVersion,
            prediction = Prediction(
                predictions = predictions.map { item ->
                    WordPrediction(
                        text = item.text,
                        probabilities = item.probabilities,
                        box = PredictionBox(
                            x = item.box.x,
                            y = item.box.y,
                            w = item.box.w,
                            h = item.box.h,
                        ),
                        line = item.line,
                        gloss = PredictionGloss(
                            lemma = item.gloss?.lemma.orEmpty(),
                            translation = item.gloss?.translation.orEmpty(),
                            definition = item.gloss?.definition.orEmpty(),
                        ),
                    )
                },
                modelVersion = modelVersion,
            ),
            imageBytes = imageBytes,
        )
    }
}
