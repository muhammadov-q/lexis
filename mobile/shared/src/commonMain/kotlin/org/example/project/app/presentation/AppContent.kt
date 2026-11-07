package org.example.project.app.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.app.navigation.MainNavigation
import org.example.project.app.presentation.composables.AppTopBar
import org.example.project.app.presentation.composables.NavigationBottomBar
import org.example.project.common.presentation.ui.helpers.asString
import org.example.project.common.presentation.ui.theme.ScriptLensColors
import org.example.project.common.presentation.ui.theme.ScriptLensTheme
import org.example.project.core.navigation.AppRoutes
import org.example.project.core.navigation.Screen
import org.example.project.core.navigation.navigateAuth
import org.example.project.core.navigation.navigateCameraRoot
import org.example.project.feature.auth.presentation.AuthMode
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AppContent() {
    val appViewModel: AppViewModel = koinViewModel()
    val appState by appViewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = appState.error?.asString()

    LaunchedEffect(appViewModel, navController) {
        appViewModel.effects.collect { effect ->
            when (effect) {
                is AppEffect.NavigateToAuth -> navController.navigateAuth(effect.mode)
                is AppEffect.NavigateToDashboard -> navController.navigateCameraRoot()
                is AppEffect.NavigateToCameraRoot -> navController.navigateCameraRoot()
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            appViewModel.onEvent(AppEvent.DismissErrorRequested)
        }
    }

    AppScaffold(
        state = appState,
        currentRoute = currentRoute,
        canNavigateBack = AppRoutes.showsBackButton(currentRoute, navController.previousBackStackEntry != null),
        snackbarHostState = snackbarHostState,
        onEvent = appViewModel::onEvent,
        onBack = { navController.popBackStack() },
        onDestinationSelected = { selectedScreen ->
            navController.navigate(selectedScreen.route) {
                popUpTo(Screen.CameraView.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
    ) { paddingValues ->
        if (appState.isSessionChecking) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            MainNavigation(
                navController = navController,
                isAuthenticated = appState.isAuthenticated,
                onEvent = appViewModel::onEvent,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun AppScaffold(
    state: AppContract,
    currentRoute: String?,
    canNavigateBack: Boolean,
    snackbarHostState: SnackbarHostState,
    onEvent: (AppEvent) -> Unit,
    onBack: () -> Unit,
    onDestinationSelected: (Screen) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = ScriptLensColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentRoute !in AppRoutes.AUTH_ROUTES && (!state.isSessionChecking || canNavigateBack)) {
                AppTopBar(
                    isAuthenticated = state.isAuthenticated,
                    isSigningOut = state.isSigningOut,
                    showSessionActions = !state.isSessionChecking,
                    canNavigateBack = canNavigateBack,
                    onBack = onBack,
                    onSignInRequested = { onEvent(AppEvent.SignInRequested(AuthMode.SignIn)) },
                    onSignOutRequested = { onEvent(AppEvent.SignOutRequested) },
                )
            }
        },
        bottomBar = {
            if (!state.isSessionChecking && currentRoute !in AppRoutes.AUTH_ROUTES) {
                NavigationBottomBar(
                    onClick = onDestinationSelected,
                    currentRoute = AppRoutes.bottomBarRoute(currentRoute),
                    destinations = listOf(Screen.CameraView, Screen.Docs, Screen.Source),
                )
            }
        },
        content = content
    )
}

@Preview(showBackground = true)
@Composable
private fun AppContentPreview() {
    ScriptLensTheme {
        AppScaffold(
            state = AppContract(isAuthenticated = false, isSessionChecking = false),
            currentRoute = Screen.CameraView.route,
            canNavigateBack = false,
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onBack = {},
            onDestinationSelected = {},
        ) { paddingValues ->

        }
    }
}
