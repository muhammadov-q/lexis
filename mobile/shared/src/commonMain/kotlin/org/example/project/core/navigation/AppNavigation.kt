package org.example.project.core.navigation

import androidx.navigation.NavHostController
import org.example.project.feature.auth.presentation.AuthMode

object AppRoutes {
    const val AUTH_SIGN_IN = "auth/sign-in"
    const val AUTH_SIGN_UP = "auth/sign-up"
    const val HISTORY_DETAIL_ID = "id"
    const val HISTORY_DETAIL_WORD = "word"
    const val HISTORY_DETAIL = "docs/{$HISTORY_DETAIL_ID}?$HISTORY_DETAIL_WORD={$HISTORY_DETAIL_WORD}"
    const val VOCABULARY_WORD_ID = "id"
    const val VOCABULARY_WORD = "vocabulary/{$VOCABULARY_WORD_ID}"
    const val NO_WORD = -1

    val AUTH_ROUTES = setOf(AUTH_SIGN_IN, AUTH_SIGN_UP)
    private val nestedRoutes = setOf(HISTORY_DETAIL, VOCABULARY_WORD)

    fun showsBackButton(route: String?, hasPreviousDestination: Boolean): Boolean =
        hasPreviousDestination && route in nestedRoutes

    fun historyDetail(id: Long, wordIndex: Int? = null): String {
        val path = "docs/$id"
        return if (wordIndex == null) path else "$path?$HISTORY_DETAIL_WORD=$wordIndex"
    }

    fun vocabularyWord(id: Long): String = "vocabulary/$id"

    fun bottomBarRoute(route: String?): String? = when (route) {
        HISTORY_DETAIL -> Screen.Docs.route
        VOCABULARY_WORD -> Screen.Vocabulary.route
        else -> route
    }
}

fun NavHostController.navigateHistoryDetail(id: Long) {
    navigate(AppRoutes.historyDetail(id)) { launchSingleTop = true }
}

fun NavHostController.navigateHistoryWord(id: Long, wordIndex: Int) {
    navigate(AppRoutes.historyDetail(id, wordIndex))
}

fun NavHostController.navigateVocabularyWord(id: Long) {
    navigate(AppRoutes.vocabularyWord(id)) { launchSingleTop = true }
}

fun NavHostController.navigateAuth(mode: AuthMode) {
    val route = when (mode) {
        AuthMode.SignIn -> AppRoutes.AUTH_SIGN_IN
        AuthMode.SignUp -> AppRoutes.AUTH_SIGN_UP
    }
    navigate(route)
}

fun NavHostController.navigateDashboard() {
    navigateCameraRoot()
}

fun NavHostController.navigateCameraRoot() {
    navigate(Screen.CameraView.route) {
        popUpTo(Screen.CameraView.route) { inclusive = false }
        launchSingleTop = true
    }
}
