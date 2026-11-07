package org.example.project.feature.history.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.common.presentation.ui.helpers.UiText
import org.example.project.feature.auth.domain.model.AuthApiException
import org.example.project.feature.auth.domain.model.User
import org.example.project.feature.auth.domain.session.AuthSessionStore
import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.PredictionBox
import org.example.project.feature.camera.domain.model.PredictionGloss
import org.example.project.feature.camera.domain.model.WordPrediction
import org.example.project.feature.history.domain.model.RecognitionDetail
import org.example.project.feature.history.domain.model.RecognitionHistoryItem
import org.example.project.feature.history.domain.repository.HistoryRepository
import org.example.project.feature.history.domain.session.HistoryRefreshStore
import org.example.project.feature.history.domain.usecase.GetRecognitionDetailUseCase
import org.example.project.feature.history.domain.usecase.GetRecognitionHistoryUseCase
import org.example.project.feature.historydetail.presentation.HistoryDetailContract
import org.example.project.feature.historydetail.presentation.HistoryDetailViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @Test
    fun loadRequestedPopulatesHistoryItems() = runHistoryTest {
        val items = listOf(
            RecognitionHistoryItem(1, "2026-01-01", "image1.jpg", "v1", "alpha beta"),
            RecognitionHistoryItem(2, "2026-01-02", "image2.jpg", "v1", "gamma delta"),
        )
        val viewModel = createViewModel(FakeHistoryRepository(historyItems = items))

        viewModel.onEvent(HistoryEvent.LoadRequested)
        advanceUntilIdle()

        assertEquals(items, viewModel.state.value.items)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun loadRequestedShowsLoadingStateWhileRequestInFlight() = runHistoryTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeHistoryRepository(
            historyProvider = {
                gate.await()
                emptyList()
            },
        )
        val viewModel = createViewModel(repository)

        viewModel.onEvent(HistoryEvent.LoadRequested)
        runCurrent()
        assertTrue(viewModel.state.value.isLoading)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun loadRequestedMapsUnauthorizedErrorForGuests() = runHistoryTest {
        val viewModel = createViewModel(
            FakeHistoryRepository(historyError = AuthApiException(401)),
        )

        viewModel.onEvent(HistoryEvent.LoadRequested)
        advanceUntilIdle()

        assertEquals(UiText.Plain("Sign in to load your recognition history."), viewModel.state.value.error)
        assertTrue(viewModel.state.value.requiresSignIn)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun loadRequestedMapsGeneralErrors() = runHistoryTest {
        val viewModel = createViewModel(
            FakeHistoryRepository(historyError = IllegalStateException("offline")),
        )

        viewModel.onEvent(HistoryEvent.LoadRequested)
        advanceUntilIdle()

        assertEquals(UiText.Plain("Could not load recognition history. Please try again."), viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun openDetailRequestedLoadsSelectedDetail() = runHistoryTest {
        val detail = RecognitionDetail(
            id = 8,
            createdAt = "2026-01-05",
            filename = "result.jpg",
            modelVersion = "v2",
            prediction = samplePrediction(),
            imageBytes = byteArrayOf(1, 2, 3),
        )
        val repository = FakeHistoryRepository(detail = detail)
        val viewModel = createDetailViewModel(repository)

        viewModel.load(8)
        advanceUntilIdle()

        assertEquals(detail, viewModel.state.value.selectedDetail)
        assertEquals(8L, repository.lastDetailId)
        assertEquals(0, viewModel.state.value.selectedPredictionIndex)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun openDetailNotFoundMapsToNotFoundMessage() = runHistoryTest {
        val viewModel = createDetailViewModel(FakeHistoryRepository(detailError = AuthApiException(404)))

        viewModel.load(99)
        advanceUntilIdle()

        assertEquals(UiText.Plain("Saved recognition was not found."), viewModel.state.value.error)
    }

    @Test
    fun unauthorizedDetailRequestClearsSession() = runHistoryTest {
        val session = AuthSessionStore()
        session.setAuthenticated(User(1, "user@example.com", null, null))
        val repository = FakeHistoryRepository(detailError = AuthApiException(401))
        val viewModel = HistoryDetailViewModel(GetRecognitionDetailUseCase(repository), session)

        viewModel.load(99)
        advanceUntilIdle()

        assertFalse(session.isAuthenticated())
        assertNull(viewModel.state.value.selectedDetail)
    }

    @Test
    fun dismissEventClearsError() = runHistoryTest {
        val viewModel = createViewModel(
            FakeHistoryRepository(historyError = IllegalArgumentException("boom")),
        )

        viewModel.onEvent(HistoryEvent.LoadRequested)
        advanceUntilIdle()
        viewModel.onEvent(HistoryEvent.DismissErrorRequested)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun retryUsesOpenDetailWhenDetailWasOpened() = runHistoryTest {
        val repository = FakeHistoryRepository(detailError = AuthApiException(404))
        val viewModel = createDetailViewModel(repository)

        viewModel.load(77)
        advanceUntilIdle()
        assertEquals(1, repository.detailRequests)

        repository.detailError = null
        viewModel.load(77)
        advanceUntilIdle()

        assertEquals(2, repository.detailRequests)
    }

    @Test
    fun selectPredictionUpdatesSelectedIndex() = runHistoryTest {
        val detail = RecognitionDetail(
            id = 3,
            createdAt = "2026-01-03",
            filename = "detail.jpg",
            modelVersion = "v1",
            prediction = samplePrediction(),
            imageBytes = byteArrayOf(7),
        )
        val viewModel = createDetailViewModel(FakeHistoryRepository(detail = detail))

        viewModel.load(3)
        advanceUntilIdle()
        viewModel.selectPrediction(1)

        assertEquals(1, viewModel.state.value.selectedPredictionIndex)
    }

    @Test
    fun signOutCancelsDetailLoadAndClearsState() = runHistoryTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeHistoryRepository(detailProvider = {
            gate.await()
            RecognitionDetail(3, "2026-01-03", "detail.jpg", "v1", samplePrediction(), byteArrayOf(7))
        })
        val session = AuthSessionStore()
        session.setAuthenticated(User(1, "user@example.com", null, null))
        val viewModel = HistoryDetailViewModel(GetRecognitionDetailUseCase(repository), session)

        viewModel.load(3)
        runCurrent()
        assertTrue(viewModel.state.value.isLoading)

        session.setGuest()
        runCurrent()
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(HistoryDetailContract(), viewModel.state.value)
    }

    private fun TestScope.createViewModel(repository: FakeHistoryRepository): HistoryViewModel = HistoryViewModel(
        getRecognitionHistory = GetRecognitionHistoryUseCase(repository),
        authSessionStore = AuthSessionStore(),
        historyRefreshStore = HistoryRefreshStore(),
    )

    private fun TestScope.createDetailViewModel(repository: FakeHistoryRepository): HistoryDetailViewModel {
        val session = AuthSessionStore()
        session.setAuthenticated(User(1, "user@example.com", null, null))
        return HistoryDetailViewModel(GetRecognitionDetailUseCase(repository), session)
    }

    private fun runHistoryTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class FakeHistoryRepository(
    private val historyItems: List<RecognitionHistoryItem> = emptyList(),
    private val detail: RecognitionDetail = RecognitionDetail(
        id = 1,
        createdAt = "2026-01-01",
        filename = "default.jpg",
        modelVersion = "v1",
        prediction = samplePrediction(),
        imageBytes = byteArrayOf(1),
    ),
    var historyError: Throwable? = null,
    var detailError: Throwable? = null,
    private val historyProvider: (suspend () -> List<RecognitionHistoryItem>)? = null,
    private val detailProvider: (suspend () -> RecognitionDetail)? = null,
) : HistoryRepository {
    var detailRequests: Int = 0
    var lastDetailId: Long? = null

    override suspend fun getHistory(): List<RecognitionHistoryItem> {
        historyError?.let { throw it }
        return historyProvider?.invoke() ?: historyItems
    }

    override suspend fun getRecognitionDetail(id: Long): RecognitionDetail {
        detailRequests += 1
        lastDetailId = id
        detailError?.let { throw it }
        return detailProvider?.invoke() ?: detail
    }
}

private fun samplePrediction() = Prediction(
    predictions = listOf(
        WordPrediction(
            text = "λόγος",
            probabilities = mapOf("λόγος" to 0.9),
            box = PredictionBox(1.0, 2.0, 3.0, 4.0),
            line = 0,
            gloss = PredictionGloss("λόγος", "word", "A word"),
        ),
        WordPrediction(
            text = "ἐστίν",
            probabilities = mapOf("ἐστίν" to 0.8),
            box = PredictionBox(5.0, 6.0, 7.0, 8.0),
            line = 0,
            gloss = PredictionGloss("εἰμί", "is", "To be"),
        ),
    ),
    modelVersion = "v1",
)
