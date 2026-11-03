package org.example.project.feature.camera.domain.usecase

import kotlinx.coroutines.test.runTest
import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.PredictionBox
import org.example.project.feature.camera.domain.model.PredictionGloss
import org.example.project.feature.camera.domain.model.WordPrediction
import org.example.project.feature.camera.domain.repository.PhotoRepository
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UploadPhotoUseCaseTest {
    @Test
    fun invokeDelegatesToRepositoryAndReturnsPrediction() = runTest {
        val expectedPrediction = samplePrediction()
        var receivedPhoto: ByteArray? = null
        val repository = object : PhotoRepository {
            override suspend fun uploadPhoto(photo: ByteArray): Prediction {
                receivedPhoto = photo
                return expectedPrediction
            }
        }
        val useCase = UploadPhotoUseCase(repository)
        val photo = byteArrayOf(1, 2, 3)

        val result = useCase(photo)

        assertContentEquals(photo, receivedPhoto)
        assertEquals(expectedPrediction, result)
    }

    @Test
    fun invokePropagatesRepositoryExceptions() = runTest {
        val expected = IllegalStateException("upload failed")
        val repository = object : PhotoRepository {
            override suspend fun uploadPhoto(photo: ByteArray): Prediction {
                throw expected
            }
        }
        val useCase = UploadPhotoUseCase(repository)

        val actual = assertFailsWith<IllegalStateException> { useCase(byteArrayOf(7)) }

        assertEquals(expected.message, actual.message)
    }

    private fun samplePrediction() = Prediction(
        predictions = listOf(
            WordPrediction(
                text = "λόγος",
                probabilities = mapOf("λόγος" to 0.97),
                box = PredictionBox(x = 10.0, y = 20.0, w = 30.0, h = 40.0),
                line = 1,
                gloss = PredictionGloss(
                    lemma = "λόγος",
                    translation = "word",
                    definition = "A word.",
                ),
            ),
        ),
        modelVersion = "v2",
    )
}


