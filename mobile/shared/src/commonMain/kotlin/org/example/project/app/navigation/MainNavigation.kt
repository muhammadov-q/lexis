package org.example.project.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import org.example.project.app.presentation.AppEvent
import org.example.project.core.navigation.AppRoutes
import org.example.project.core.navigation.Screen
import org.example.project.core.navigation.navigateCameraRoot
import org.example.project.core.navigation.navigateHistoryDetail
import org.example.project.core.navigation.navigateHistoryWord
import org.example.project.core.navigation.navigateVocabularyWord
import org.example.project.feature.auth.presentation.AuthMode
import org.example.project.feature.auth.presentation.AuthScreen
import org.example.project.feature.camera.presentation.CameraScreen
import org.example.project.feature.camera.presentation.CameraViewModel
import org.example.project.feature.history.presentation.HistoryScreen
import org.example.project.feature.historydetail.presentation.HistoryDetailScreen
import org.example.project.feature.historydetail.presentation.HistoryDetailViewModel
import org.example.project.feature.history.presentation.HistoryViewModel
import org.example.project.feature.source.presentation.SourceScreen
import org.example.project.feature.vocabulary.presentation.list.VocabularyScreen
import org.example.project.feature.vocabulary.presentation.list.VocabularyViewModel
import org.example.project.feature.vocabulary.presentation.word.VocabularyWordScreen
import org.example.project.feature.vocabulary.presentation.word.VocabularyWordViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun MainNavigation(
    navController: NavHostController,
    isAuthenticated: Boolean,
    onEvent: (AppEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.CameraView.route,
        modifier = modifier,
    ) {
        composable(Screen.CameraView.route) {
            val cameraViewModel: CameraViewModel = koinViewModel()
            CameraScreen(
                viewModel = cameraViewModel,
                isAuthenticated = isAuthenticated,
                onOpenSaved = navController::navigateHistoryDetail,
                onSignInRequested = { onEvent(AppEvent.SignInRequested(AuthMode.SignIn)) },
                onOpenVocabularyWord = navController::navigateVocabularyWord,
            )
        }
        composable(Screen.Docs.route) {
            val historyViewModel: HistoryViewModel = koinViewModel()
            HistoryScreen(
                isAuthenticated = isAuthenticated,
                viewModel = historyViewModel,
                onSignInRequested = { onEvent(AppEvent.SignInRequested(AuthMode.SignIn)) },
                onCreateAccountRequested = { onEvent(AppEvent.SignInRequested(AuthMode.SignUp)) },
                onReadPageRequested = navController::navigateCameraRoot,
                onOpenDetail = navController::navigateHistoryDetail,
            )
        }
        composable(
            route = AppRoutes.HISTORY_DETAIL,
            arguments = listOf(
                navArgument(AppRoutes.HISTORY_DETAIL_ID) { type = NavType.LongType },
                navArgument(AppRoutes.HISTORY_DETAIL_WORD) {
                    type = NavType.IntType
                    defaultValue = AppRoutes.NO_WORD
                },
            ),
        ) { entry ->
            val arguments = requireNotNull(entry.arguments)
            val id = arguments.read { getLong(AppRoutes.HISTORY_DETAIL_ID) }
            val word = arguments.read { getInt(AppRoutes.HISTORY_DETAIL_WORD) }
            val viewModel: HistoryDetailViewModel = koinViewModel()
            HistoryDetailScreen(
                id = id,
                isAuthenticated = isAuthenticated,
                viewModel = viewModel,
                onSignInRequested = { onEvent(AppEvent.SignInRequested(AuthMode.SignIn)) },
                onCreateAccountRequested = { onEvent(AppEvent.SignInRequested(AuthMode.SignUp)) },
                initialWordIndex = word.takeIf { it != AppRoutes.NO_WORD },
                onOpenVocabularyWord = navController::navigateVocabularyWord,
            )
        }
        composable(Screen.Vocabulary.route) {
            val viewModel: VocabularyViewModel = koinViewModel()
            VocabularyScreen(
                isAuthenticated = isAuthenticated,
                viewModel = viewModel,
                onSignInRequested = { onEvent(AppEvent.SignInRequested(AuthMode.SignIn)) },
                onCreateAccountRequested = { onEvent(AppEvent.SignInRequested(AuthMode.SignUp)) },
                onOpenHistory = {
                    navController.navigate(Screen.Docs.route) {
                        popUpTo(Screen.CameraView.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenWord = navController::navigateVocabularyWord,
            )
        }
        composable(
            route = AppRoutes.VOCABULARY_WORD,
            arguments = listOf(navArgument(AppRoutes.VOCABULARY_WORD_ID) { type = NavType.LongType }),
        ) { entry ->
            val id = requireNotNull(entry.arguments).read { getLong(AppRoutes.VOCABULARY_WORD_ID) }
            val viewModel: VocabularyWordViewModel = koinViewModel()
            VocabularyWordScreen(
                id = id,
                isAuthenticated = isAuthenticated,
                viewModel = viewModel,
                onSignInRequested = { onEvent(AppEvent.SignInRequested(AuthMode.SignIn)) },
                onCreateAccountRequested = { onEvent(AppEvent.SignInRequested(AuthMode.SignUp)) },
                onOpenSource = navController::navigateHistoryWord,
                onRemoved = { navController.popBackStack() },
            )
        }
        composable(Screen.Source.route) {
            SourceScreen()
        }
        composable(AppRoutes.AUTH_SIGN_IN) {
            AuthScreen(
                initialMode = AuthMode.SignIn,
                onGuestContinue = { onEvent(AppEvent.GuestContinueRequested) },
            )
        }
        composable(AppRoutes.AUTH_SIGN_UP) {
            AuthScreen(
                initialMode = AuthMode.SignUp,
                onGuestContinue = { onEvent(AppEvent.GuestContinueRequested) },
            )
        }
    }
}
