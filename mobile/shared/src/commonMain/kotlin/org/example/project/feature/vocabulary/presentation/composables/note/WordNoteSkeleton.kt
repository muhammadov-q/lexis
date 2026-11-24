package org.example.project.feature.vocabulary.presentation.composables.note

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme

@Composable
internal fun WordNoteSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_12)) {
        SkeletonBlock(Modifier.width(40.dp).height(16.dp))
        SkeletonBlock(Modifier.fillMaxWidth().height(96.dp))
        SkeletonBlock(Modifier.width(112.dp).height(40.dp))
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier) {
    Box(modifier = modifier.clip(MaterialTheme.shapes.medium).background(LexisColors.Muted))
}

@Preview(showBackground = true)
@Composable
private fun WordNoteSkeletonPreview() {
    LexisTheme {
        WordNoteSkeleton()
    }
}
