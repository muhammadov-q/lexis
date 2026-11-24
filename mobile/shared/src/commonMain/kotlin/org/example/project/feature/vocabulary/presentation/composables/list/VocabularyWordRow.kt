package org.example.project.feature.vocabulary.presentation.composables.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.vocabulary_no_translation
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.feature.vocabulary.domain.model.VocabularyWord
import org.example.project.feature.vocabulary.presentation.composables.common.previewVocabularyWord
import org.example.project.feature.vocabulary.presentation.composables.common.sourceLabel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VocabularyWordRow(word: VocabularyWord, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LexisSpacing.padding_12))
            .background(LexisColors.Muted)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = LexisSpacing.padding_16, vertical = LexisSpacing.padding_12),
        horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = word.word,
                style = LexisTypography.Body.copy(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                color = LexisColors.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = sourceLabel(word.source),
                style = LexisTypography.Annotation,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = word.gloss?.translation?.takeIf { it.isNotBlank() }
                ?: stringResource(Res.string.vocabulary_no_translation),
            style = LexisTypography.BodySmall,
            color = LexisColors.Subtext,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 160.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VocabularyWordRowPreview() {
    LexisTheme {
        VocabularyWordRow(word = previewVocabularyWord, onClick = {})
    }
}
