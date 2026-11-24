package org.example.project.feature.vocabulary.presentation.composables.word

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.ic_arrow_down
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.feature.vocabulary.domain.model.WordSource
import org.example.project.feature.vocabulary.presentation.composables.common.previewVocabularyWord
import org.example.project.feature.vocabulary.presentation.composables.common.sourceLabel
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SourceLink(source: WordSource, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = sourceLabel(source),
            style = LexisTypography.BodySmall.copy(textDecoration = TextDecoration.Underline),
            color = LexisColors.Subtext,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_down),
            contentDescription = null,
            tint = LexisColors.Subtext,
            modifier = Modifier.size(16.dp).rotate(-90f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SourceLinkPreview() {
    LexisTheme {
        SourceLink(source = previewVocabularyWord.source, onClick = {})
    }
}
