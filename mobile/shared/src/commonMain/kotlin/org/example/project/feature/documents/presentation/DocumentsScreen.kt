package org.example.project.feature.documents.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.common.presentation.ui.composables.webview.WebView

private const val DOCS_URL = "https://github.com/muhammadov-q/lexis/tree/develop/docs"

@Composable
fun DocumentsScreen() {
    WebView(url = DOCS_URL, modifier = Modifier.fillMaxSize())
}

