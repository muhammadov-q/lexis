package org.example.project.feature.vocabulary.presentation.composables.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme

@Composable
internal fun VocabularySkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_8)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(LexisSpacing.padding_12))
                    .background(LexisColors.Muted),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VocabularySkeletonPreview() {
    LexisTheme {
        VocabularySkeleton()
    }
}
