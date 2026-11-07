package org.example.project

import androidx.compose.runtime.Composable
import org.example.project.app.presentation.AppContent
import org.example.project.common.presentation.ui.theme.ScriptLensTheme
import org.example.project.di.appModule
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        ScriptLensTheme {
            AppContent()
        }
    }
}
