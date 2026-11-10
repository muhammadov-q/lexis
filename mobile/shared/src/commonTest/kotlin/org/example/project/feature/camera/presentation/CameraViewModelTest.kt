package org.example.project.feature.camera.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.feature.camera.domain.model.ApiException
import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.OcrModel
import org.example.project.feature.camera.domain.model.OcrModels
import org.example.project.feature.camera.domain.model.PredictionBox
import org.example.project.feature.camera.domain.model.PredictionGloss
import org.example.project.feature.camera.domain.model.WordPrediction
import org.example.project.feature.camera.domain.repository.PhotoRepository
import org.example.project.feature.camera.domain.usecase.UploadPhotoUseCase
import org.example.project.feature.camera.domain.usecase.GetModelsUseCase
import org.example.project.feature.camera.presentation.models.CameraScreenErrorState
import org.example.project.feature.camera.presentation.models.CameraScreenState
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {
    @Test
    fun discoveryRetryAndReadAgainPreservePhotoAndUseSelectedModel() = runCameraTest {
        val gate = CompletableDeferred<Unit>()
        var loads = 0
        val uploads = mutableListOf<Pair<ByteArray, String?>>()
        val models = listOf(OcrModel("first", "First", "One"), OcrModel("second", "Second", "Two"))
        val repository = object : PhotoRepository {
            override suspend fun getModels(): OcrModels {
                loads++
                if (loads == 1) {
                    gate.await()
                    throw ApiException(503)
                }
                return OcrModels(models, "first")
            }
            override suspend fun uploadPhoto(photo: ByteArray, model: String?): Prediction {
                uploads += photo to model
                return samplePrediction()
            }
        }
        val viewModel = CameraViewModel(UploadPhotoUseCase(repository), GetModelsUseCase(repository))
        runCurrent()
        assertTrue(viewModel.state.value.modelsLoading)
        gate.complete(Unit)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.modelsError)
        assertNull(viewModel.state.value.selectedModel)
        val photo = byteArrayOf(4, 5)
        viewModel.onEvent(CameraEvent.OnPhotoPicked(photo))
        advanceUntilIdle()
        assertNull(uploads.single().second)

        viewModel.onEvent(CameraEvent.ReadAgain)
        assertNull(viewModel.state.value.prediction)
        assertContentEquals(photo, viewModel.state.value.photo)
        viewModel.onEvent(CameraEvent.ReloadModels)
        advanceUntilIdle()
        assertEquals("first", viewModel.state.value.selectedModel)
        viewModel.onEvent(CameraEvent.SelectModel("second"))
        viewModel.onEvent(CameraEvent.OnPhotoPicked(photo))
        advanceUntilIdle()
        assertEquals("second", uploads.last().second)
        assertContentEquals(photo, uploads.last().first)
        assertEquals(2, uploads.size)
    }

    @Test
    fun emptyModelListFallsBackToDefault() = runCameraTest {
        val repository = object : PhotoRepository {
            override suspend fun getModels() = OcrModels(emptyList(), "missing")
            override suspend fun uploadPhoto(photo: ByteArray, model: String?) = samplePrediction()
        }
        val viewModel = CameraViewModel(UploadPhotoUseCase(repository), GetModelsUseCase(repository))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.modelsError)
        assertNull(viewModel.state.value.selectedModel)
    }

    @Test
    fun selectionIsLockedDuringUploadAndValidationErrorKeepsTheImage() = runCameraTest {
        val gate = CompletableDeferred<Unit>()
        val repository = object : PhotoRepository {
            override suspend fun getModels() = OcrModels(
                listOf(OcrModel("a", "A", "A"), OcrModel("b", "B", "B")), "a",
            )
            override suspend fun uploadPhoto(photo: ByteArray, model: String?): Prediction {
                gate.await()
                throw ApiException(422)
            }
        }
        val viewModel = CameraViewModel(UploadPhotoUseCase(repository), GetModelsUseCase(repository))
        advanceUntilIdle()
        val photo = byteArrayOf(7)
        viewModel.onEvent(CameraEvent.OnPhotoPicked(photo))
        runCurrent()
        viewModel.onEvent(CameraEvent.SelectModel("b"))
        assertEquals("a", viewModel.state.value.selectedModel)
        gate.complete(Unit)
        advanceUntilIdle()
        val error = assertIs<CameraScreenState.Error>(viewModel.state.value.cameraScreenState)
        assertIs<CameraScreenErrorState.ValidationFailed>(error.error)
        viewModel.onEvent(CameraEvent.ReadAgain)
        viewModel.onEvent(CameraEvent.SelectModel("b"))
        assertEquals("b", viewModel.state.value.selectedModel)
        assertContentEquals(photo, viewModel.state.value.photo)
    }

    @Test
    fun changingModelAfterRecognitionReuploadsPhotoAndReloadPreservesSelection() = runCameraTest {
        val models = listOf(
            OcrModel("first", "First", "First model"),
            OcrModel("second", "Second", "Second model"),
        )
        val uploads = mutableListOf<Pair<ByteArray, String?>>()
        var modelLoads = 0
        val repository = object : PhotoRepository {
            override suspend fun getModels(): OcrModels {
                modelLoads++
                return OcrModels(models, "first")
            }

            override suspend fun uploadPhoto(photo: ByteArray, model: String?): Prediction {
                uploads += photo to model
                return samplePrediction(modelVersion = model ?: "default")
            }
        }
        val viewModel = CameraViewModel(UploadPhotoUseCase(repository), GetModelsUseCase(repository))
        val photo = byteArrayOf(3, 4, 5)
        advanceUntilIdle()

        viewModel.onEvent(CameraEvent.OnPhotoPicked(photo))
        advanceUntilIdle()
        viewModel.onEvent(CameraEvent.SelectModel("second"))
        advanceUntilIdle()

        assertEquals(2, uploads.size)
        assertContentEquals(photo, uploads[1].first)
        assertEquals("second", uploads[1].second)
        assertEquals("second", viewModel.state.value.selectedModel)
        assertEquals("second", viewModel.state.value.prediction?.modelVersion)

        viewModel.onEvent(CameraEvent.SelectModel("unknown"))
        assertEquals(2, uploads.size)
        assertEquals("second", viewModel.state.value.selectedModel)

        viewModel.onEvent(CameraEvent.ReloadModels)
        advanceUntilIdle()
        assertEquals(2, modelLoads)
        assertEquals("second", viewModel.state.value.selectedModel)
    }

    @Test
    fun capturePhotoRequestedEmitsEffectWithoutChangingState() = runCameraTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CameraEvent.CapturePhotoRequested)

        assertEquals(CameraEffect.CapturePhoto, viewModel.effects.first())
        assertEquals(CameraScreenState.NoPhotoCaptured, viewModel.state.value.cameraScreenState)
        assertNull(viewModel.state.value.prediction)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun selectPhotoRequestedEmitsEffect() = runCameraTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CameraEvent.SelectPhotoRequested)

        assertEquals(CameraEffect.SelectPhoto, viewModel.effects.first())
    }

    @Test
    fun photoCaptureFailureUpdatesErrorState() = runCameraTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CameraEvent.OnPhotoCaptureFailed("Camera unavailable"))

        val screenState = viewModel.state.value.cameraScreenState
        assertIs<CameraScreenState.Error>(screenState)
        assertIs<CameraScreenErrorState.PhotoUnavailable>(screenState.error)
        assertEquals(UiText.Plain("Camera unavailable"), screenState.error.message)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun cancellingCaptureClearsLoadingWithoutChangingScreenState() = runCameraTest {
        val viewModel = createViewModel()
        viewModel.onEvent(CameraEvent.OnImagePickerProcessingStarted)
        assertTrue(viewModel.state.value.isLoading)

        viewModel.onEvent(CameraEvent.OnPhotoCaptureCancelled)

        assertEquals(CameraScreenState.NoPhotoCaptured, viewModel.state.value.cameraScreenState)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun pickerProcessingStartedSetsLoading() = runCameraTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CameraEvent.OnImagePickerProcessingStarted)

        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun pickedPhotoWithoutDataShowsUnavailableError() = runCameraTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CameraEvent.OnPhotoPicked(null))
        val state = viewModel.state.value

        val screenState = state.cameraScreenState
        assertIs<CameraScreenState.Error>(screenState)
        assertIs<CameraScreenErrorState.PhotoUnavailable>(screenState.error)
        assertFalse(state.isLoading)
        assertNull(state.photo)
    }

    @Test
    fun pickedEmptyPhotoDataShowsUnavailableError() = runCameraTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CameraEvent.OnPhotoPicked(byteArrayOf()))

        val screenState = viewModel.state.value.cameraScreenState
        assertIs<CameraScreenState.Error>(screenState)
        assertIs<CameraScreenErrorState.PhotoUnavailable>(screenState.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun successfulUploadDisplaysPredictionAndClearsLoading() = runCameraTest {
        val photo = byteArrayOf(1, 2, 3)
        var uploads = 0
        var uploadedPhoto: ByteArray? = null
        val prediction = samplePrediction()
        val viewModel = createViewModel {
            uploads++
            uploadedPhoto = it
            prediction
        }

        viewModel.onEvent(CameraEvent.OnPhotoPicked(photo))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, uploads)
        assertContentEquals(photo, uploadedPhoto)
        assertContentEquals(photo, state.photo)
        assertIs<CameraScreenState.PhotoCaptured>(state.cameraScreenState)
        assertFalse(state.isLoading)
        assertEquals("v1", state.prediction?.modelVersion)
        assertEquals(2, state.prediction?.predictions?.size)
        assertNull(state.selectedPredictionIndex)
    }

    @Test
    fun selectingAnotherWordUpdatesSelectedIndexWithoutUploadingAgain() = runCameraTest {
        var uploads = 0
        val viewModel = createViewModel {
            uploads++
            samplePrediction()
        }

        viewModel.onEvent(CameraEvent.OnPhotoPicked(byteArrayOf(1, 2, 3)))
        advanceUntilIdle()
        assertEquals(1, uploads)
        assertNull(viewModel.state.value.selectedPredictionIndex)

        viewModel.onEvent(CameraEvent.SelectPrediction(1))
        advanceUntilIdle()

        assertEquals(1, uploads)
        assertEquals(1, viewModel.state.value.selectedPredictionIndex)
    }

    @Test
    fun choosingNewImageClearsOldPredictionAndSelectionBeforeNextUploadCompletes() = runCameraTest {
        val firstPrediction = samplePrediction()
        val secondPrediction = samplePrediction(modelVersion = "v2")
        var uploads = 0
        val viewModel = createViewModel {
            uploads++
            if (uploads == 1) firstPrediction else secondPrediction
        }

        viewModel.onEvent(CameraEvent.OnPhotoPicked(byteArrayOf(1, 1, 1)))
        advanceUntilIdle()
        viewModel.onEvent(CameraEvent.SelectPrediction(1))
        assertEquals(1, viewModel.state.value.selectedPredictionIndex)

        viewModel.onEvent(CameraEvent.OnPhotoPicked(byteArrayOf(2, 2, 2)))

        val transientState = viewModel.state.value
        assertNull(transientState.prediction)
        assertNull(transientState.selectedPredictionIndex)
        assertTrue(transientState.isLoading)

        advanceUntilIdle()
        assertEquals(2, uploads)
        val finalState = viewModel.state.value
        assertNotNull(finalState.prediction)
        assertEquals("v2", finalState.prediction.modelVersion)
        assertNull(finalState.selectedPredictionIndex)
    }

    @Test
    fun failedUploadDisplaysMappedErrorAndClearsLoading() = runCameraTest {
        val viewModel = createViewModel {
            throw ApiException(503)
        }

        viewModel.onEvent(CameraEvent.OnPhotoPicked(byteArrayOf(9, 9, 9)))
        advanceUntilIdle()

        val state = viewModel.state.value
        val screenState = state.cameraScreenState
        assertIs<CameraScreenState.Error>(screenState)
        assertIs<CameraScreenErrorState.ServiceOffline>(screenState.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun retryAfterFailedUploadReusesPhotoAndCanSucceed() = runCameraTest {
        val photo = byteArrayOf(7, 7, 7)
        var attempts = 0
        val uploadedPhotos = mutableListOf<ByteArray>()
        val viewModel = createViewModel {
            attempts++
            uploadedPhotos += it
            if (attempts == 1) throw ApiException(503)
            samplePrediction()
        }

        viewModel.onEvent(CameraEvent.OnPhotoPicked(photo))
        advanceUntilIdle()
        assertIs<CameraScreenState.Error>(viewModel.state.value.cameraScreenState)

        viewModel.onEvent(CameraEvent.RetryUploadRequested)
        advanceUntilIdle()

        val finalState = viewModel.state.value
        assertEquals(2, attempts)
        assertContentEquals(photo, uploadedPhotos[0])
        assertContentEquals(photo, uploadedPhotos[1])
        assertContentEquals(photo, finalState.photo)
        assertIs<CameraScreenState.PhotoCaptured>(finalState.cameraScreenState)
        assertFalse(finalState.isLoading)
        assertEquals("v1", finalState.prediction?.modelVersion)
    }

    @Test
    fun reverseOrderUploadCompletionKeepsLatestPhotoState() = runCameraTest {
        val firstUpload = CompletableDeferred<Prediction>()
        val secondUpload = CompletableDeferred<Prediction>()
        var uploads = 0
        val viewModel = createViewModel {
            uploads++
            if (uploads == 1) {
                withContext(NonCancellable) { firstUpload.await() }
            } else {
                secondUpload.await()
            }
        }

        viewModel.onEvent(CameraEvent.OnPhotoPicked(byteArrayOf(1, 1, 1)))
        advanceUntilIdle()
        viewModel.onEvent(CameraEvent.OnPhotoPicked(byteArrayOf(2, 2, 2)))

        secondUpload.complete(samplePrediction(modelVersion = "v2"))
        advanceUntilIdle()

        assertEquals(2, uploads)
        assertEquals("v2", viewModel.state.value.prediction?.modelVersion)
        assertContentEquals(byteArrayOf(2, 2, 2), viewModel.state.value.photo)
        assertFalse(viewModel.state.value.isLoading)

        firstUpload.complete(samplePrediction(modelVersion = "v1"))
        advanceUntilIdle()

        assertEquals("v2", viewModel.state.value.prediction?.modelVersion)
        assertContentEquals(byteArrayOf(2, 2, 2), viewModel.state.value.photo)
    }

    @Test
    fun newUploadCancelsObsoleteInFlightUpload() = runCameraTest {
        val firstUploadCancelled = CompletableDeferred<Unit>()
        var uploads = 0
        val viewModel = createViewModel {
            uploads++
            if (uploads == 1) {
                try {
                    awaitCancellation()
                } finally {
                    firstUploadCancelled.complete(Unit)
                }
            }
            samplePrediction(modelVersion = "v2")
        }

        viewModel.onEvent(CameraEvent.OnPhotoPicked(byteArrayOf(1, 1, 1)))
        advanceUntilIdle()

        viewModel.onEvent(CameraEvent.OnPhotoPicked(byteArrayOf(2, 2, 2)))
        advanceUntilIdle()

        firstUploadCancelled.await()
        assertEquals(2, uploads)
        assertEquals("v2", viewModel.state.value.prediction?.modelVersion)
        assertContentEquals(byteArrayOf(2, 2, 2), viewModel.state.value.photo)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun retryWithoutPhotoShowsUnavailableErrorWithoutUploading() = runCameraTest {
        var uploads = 0
        val viewModel = createViewModel {
            uploads++
            error("Unexpected upload")
        }
        viewModel.onEvent(CameraEvent.OnPhotoCaptureFailed("Camera unavailable"))

        viewModel.onEvent(CameraEvent.RetryUploadRequested)
        advanceUntilIdle()

        val screenState = viewModel.state.value.cameraScreenState
        assertIs<CameraScreenState.Error>(screenState)
        assertIs<CameraScreenErrorState.PhotoUnavailable>(screenState.error)
        assertFalse(screenState.error.canRetry)
        assertEquals(0, uploads)
        assertFalse(viewModel.state.value.isLoading)
    }

    private fun samplePrediction(modelVersion: String = "v1") = Prediction(
        predictions = listOf(
            WordPrediction(
                text = "λόγος",
                probabilities = mapOf("λόγος" to 0.92, "λόγον" to 0.08),
                box = PredictionBox(x = 80.0, y = 60.0, w = 120.0, h = 180.0),
                line = 0,
                gloss = PredictionGloss(
                    lemma = "λόγος",
                    translation = "word",
                    definition = "A word or account.",
                ),
            ),
            WordPrediction(
                text = "ἐστίν",
                probabilities = mapOf("ἐστίν" to 0.84, "ἐστι" to 0.16),
                box = PredictionBox(x = 240.0, y = 60.0, w = 120.0, h = 180.0),
                line = 0,
                gloss = PredictionGloss(
                    lemma = "εἰμί",
                    translation = "is",
                    definition = "Third-person singular of to be.",
                ),
            ),
        ),
        modelVersion = modelVersion
    )

    private fun runCameraTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            advanceUntilIdle()
            Dispatchers.resetMain()
        }
    }

    private fun createViewModel(
        upload: suspend (ByteArray) -> Prediction = { error("Unexpected upload") },
    ): CameraViewModel {
        val repository = object : PhotoRepository {
            override suspend fun uploadPhoto(photo: ByteArray, model: String?): Prediction = upload(photo)
            override suspend fun getModels() = OcrModels(listOf(OcrModel("default", "Default", "Default model")), "default")
        }
        return CameraViewModel(UploadPhotoUseCase(repository), GetModelsUseCase(repository))
    }
}
