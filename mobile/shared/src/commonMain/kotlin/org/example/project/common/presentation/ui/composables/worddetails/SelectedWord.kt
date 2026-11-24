package org.example.project.common.presentation.ui.composables.worddetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.word_no_translation_message
import lexis.shared.generated.resources.word_no_translation_message_with_confidence
import lexis.shared.generated.resources.word_no_translation_title
import org.example.project.common.presentation.ui.preview.previewTranslatedPrediction
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.helpers.confidence
import org.example.project.common.presentation.helpers.formatPercent
import org.example.project.common.presentation.helpers.hasGloss
import org.example.project.common.presentation.models.WordPredictionModel
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.graphics.ImageBitmap
import org.example.project.common.presentation.ui.preview.previewPage

@Composable
internal fun SelectedWord(
    words: List<WordPredictionModel>,
    index: Int,
    image: ImageBitmap?,
    onPredictionSelected: (Int?) -> Unit,
) {
    val word = words[index]

    Column(verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_20)) {
        if (image != null && word.box.w > 0 && word.box.h > 0) {
            WordLoupe(image = image, word = word)
        }

        WordHeading(words = words, index = index, onPredictionSelected = onPredictionSelected)

        if (word.hasGloss()) {
            WordGloss(word = word)
        } else {
            val confidence = word.confidence()
            MutedNote(
                title = stringResource(Res.string.word_no_translation_title),
                message = if (confidence == null) {
                    stringResource(Res.string.word_no_translation_message)
                } else {
                    stringResource(
                        Res.string.word_no_translation_message_with_confidence,
                        formatPercent(confidence),
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedWordPreview() {
    MaterialTheme {
        SelectedWord(
            words = previewTranslatedPrediction.predictions,
            index = 1,
            image = previewPage(),
            onPredictionSelected = {},
        )
    }
}
