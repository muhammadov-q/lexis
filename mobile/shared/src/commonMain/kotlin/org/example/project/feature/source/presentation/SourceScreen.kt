package org.example.project.feature.source.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.common.presentation.ui.composables.webview.WebView
import org.example.project.common.presentation.ui.theme.ScriptLensColors

private const val SOURCE_URL = "https://github.com/muhammadov-q/team4"

@Composable
fun SourceScreen() {
    WebView(url = SOURCE_URL, modifier = Modifier.fillMaxSize().background(ScriptLensColors.Background))
}
