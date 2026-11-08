package org.example.project.core.navigation

import androidx.navigation.NavHostController
import org.example.project.feature.auth.presentation.AuthMode

object AppRoutes {
    const val AUTH_SIGN_IN = "auth/sign-in"
    const val AUTH_SIGN_UP = "auth/sign-up"
    const val HISTORY_DETAIL_ID = "id"
    const val HISTORY_DETAIL = "docs/{$HISTORY_DETAIL_ID}"

    val AUTH_ROUTES = setOf(AUTH_SIGN_IN, AUTH_SIGN_UP)
    private val nestedRoutes = setOf(HISTORY_DETAIL)

    fun showsBackButton(route: String?, hasPreviousDestination: Boolean): Boolean =
        hasPreviousDestination && route in nestedRoutes

    fun historyDetail(id: Long): String = HISTORY_DETAIL.replace("{$HISTORY_DETAIL_ID}", id.toString())

    fun bottomBarRoute(route: String?): String? =
        if (route == HISTORY_DETAIL) Screen.Docs.route else route
}

fun NavHostController.navigateHistoryDetail(id: Long) {
    navigate(AppRoutes.historyDetail(id)) { launchSingleTop = true }
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
