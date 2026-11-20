package org.example.project.feature.camera.presentation.composables.predictions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTypography
import org.example.project.feature.camera.presentation.models.PredictionGlossModel
import org.example.project.feature.camera.presentation.models.PredictionBoxModel
import org.example.project.feature.camera.presentation.models.PredictionModel
import org.example.project.feature.camera.presentation.models.WordPredictionModel
import org.example.project.feature.camera.presentation.helpers.groupPredictionLines
import org.example.project.feature.camera.presentation.helpers.isConfirmed
import org.jetbrains.compose.resources.stringResource
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.prediction_definition_label
import lexis.shared.generated.resources.prediction_empty_message
import lexis.shared.generated.resources.prediction_lemma_label
import lexis.shared.generated.resources.prediction_recognized_text_label
import lexis.shared.generated.resources.prediction_select_word_hint
import lexis.shared.generated.resources.prediction_translation_label
import lexis.shared.generated.resources.prediction_transcription_label
import lexis.shared.generated.resources.prediction_gloss_unavailable
import lexis.shared.generated.resources.prediction_unconfirmed
import org.example.project.common.presentation.ui.theme.LexisColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PredictionSummary(
    prediction: PredictionModel?,
    selectedPredictionIndex: Int?,
    onPredictionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    prediction?.let {
        val groupedWords = remember(prediction.predictions) { groupPredictionLines(prediction.predictions) }
        val selectedPrediction = selectedPredictionIndex
            ?.takeIf { index -> index in prediction.predictions.indices }
            ?.let { prediction.predictions[it] }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(LexisColors.Surface)
                .padding(LexisSpacing.padding_16)
        ) {
            Text(
                modifier = Modifier.padding(bottom = LexisSpacing.padding_8),
                text = stringResource(Res.string.prediction_transcription_label),
                color = LexisColors.Subtext
            )

            if (prediction.predictions.isEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(LexisColors.Muted)
                        .padding(LexisSpacing.padding_12),
                    text = stringResource(Res.string.prediction_empty_message),
                    color = LexisColors.Subtext,
                    style = LexisTypography.Body,
                )
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(LexisColors.Background)
                    .border(1.dp, LexisColors.Border, MaterialTheme.shapes.large)
                    .padding(LexisSpacing.padding_12),
                verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_8),
            ) {
                groupedWords.forEach { line ->
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4),
                        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_4),
                    ) {
                        line.words.forEach { indexedWord ->
                            val isSelected = indexedWord.index == selectedPredictionIndex
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LexisColors.Accent.copy(alpha = 0.25f) else LexisColors.Surface)
                                    .selectable(
                                        selected = isSelected,
                                        role = Role.Button,
                                        onClick = { onPredictionSelected(indexedWord.index) },
                                    )
                                    .padding(horizontal = LexisSpacing.padding_8, vertical = LexisSpacing.padding_4),
                            ) {
                                Text(
                                    text = indexedWord.value.text,
                                    style = if (isSelected) LexisTypography.Subtitle else LexisTypography.Body,
                                    color = LexisColors.Text,
                                )
                                if (line.translation == null && indexedWord.value.gloss.translation.isNotBlank()) {
                                    Text(
                                        text = indexedWord.value.gloss.translation,
                                        style = LexisTypography.Label,
                                        color = LexisColors.Subtext,
                                    )
                                }
                            }
                        }
                    }
                    line.translation?.let { translation ->
                        Text(
                            text = translation,
                            style = LexisTypography.Body,
                            color = LexisColors.Subtext,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = LexisSpacing.padding_16)
                    .clip(MaterialTheme.shapes.large)
                    .background(LexisColors.Background)
                    .border(1.dp, LexisColors.Border, MaterialTheme.shapes.large)
                    .padding(LexisSpacing.padding_12)
            ) {
                Text(
                    text = stringResource(Res.string.prediction_translation_label),
                    style = LexisTypography.Label,
                    color = LexisColors.Subtext,
                )

                if (selectedPrediction == null) {
                    Text(
                        modifier = Modifier.padding(top = LexisSpacing.padding_8),
                        text = stringResource(Res.string.prediction_select_word_hint),
                        style = LexisTypography.Body,
                        color = LexisColors.Subtext,
                    )
                } else {
                    val unavailableText = stringResource(Res.string.prediction_gloss_unavailable)
                    Text(
                        text = selectedPrediction.gloss.translation.ifBlank { unavailableText },
                        style = LexisTypography.Heading,
                        color = LexisColors.Text,
                    )

                    PredictionDetailRow(
                        label = stringResource(Res.string.prediction_recognized_text_label),
                        value = selectedPrediction.text,
                        unavailableText = unavailableText,
                    )
                    PredictionDetailRow(
                        label = stringResource(Res.string.prediction_lemma_label),
                        value = selectedPrediction.gloss.lemma,
                        unavailableText = unavailableText,
                    )
                    PredictionDetailRow(
                        label = stringResource(Res.string.prediction_definition_label),
                        value = selectedPrediction.gloss.definition,
                        unavailableText = unavailableText,
                    )
                    if (!selectedPrediction.isConfirmed()) {
                        Text(
                            modifier = Modifier.padding(top = LexisSpacing.padding_8),
                            text = stringResource(Res.string.prediction_unconfirmed),
                            style = LexisTypography.Body,
                            color = LexisColors.Subtext,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionDetailRow(label: String, value: String, unavailableText: String) {
    Text(
        modifier = Modifier.padding(top = LexisSpacing.padding_8),
        text = label,
        style = LexisTypography.Label,
        color = LexisColors.Subtext,
    )
    Text(
        text = value.ifBlank { unavailableText },
        style = LexisTypography.Body,
        color = LexisColors.Text,
    )
}

@Preview(showBackground = true)
@Composable
private fun PredictionInfoPreview() {
    MaterialTheme {
        PredictionSummary(
            prediction = PredictionModel(
                predictions = listOf(
                    WordPredictionModel(
                        text = "logos",
                        probabilities = mapOf("logos" to 0.92),
                        box = PredictionBoxModel(80.0, 60.0, 120.0, 180.0),
                        line = 0,
                        gloss = PredictionGlossModel(
                            lemma = "logos",
                            translation = "word, speech",
                            definition = "A word, statement, or account.",
                        )
                    ),
                    WordPredictionModel(
                        text = "esti",
                        probabilities = mapOf("esti" to 0.87),
                        box = PredictionBoxModel(240.0, 60.0, 120.0, 180.0),
                        line = 0,
                        gloss = PredictionGlossModel(
                            lemma = "eimi",
                            translation = "is",
                            definition = "Third-person singular of to be.",
                        )
                    ),
                    WordPredictionModel(
                        text = "bios",
                        probabilities = mapOf("bios" to 0.95),
                        box = PredictionBoxModel(400.0, 60.0, 120.0, 180.0),
                        line = 1,
                        gloss = PredictionGlossModel(
                            lemma = "bios",
                            translation = "life",
                            definition = "Mode or course of life.",
                        )
                    ),
                ),
                modelVersion = "preview",
            ),
            selectedPredictionIndex = 0,
            onPredictionSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PredictionInfoNoSelectionPreview() {
    MaterialTheme {
        PredictionSummary(
            prediction = PredictionModel(
                predictions = listOf(
                    WordPredictionModel(
                        text = "logos",
                        probabilities = mapOf("logos" to 0.92),
                        box = PredictionBoxModel(80.0, 60.0, 120.0, 180.0),
                        line = 0,
                        gloss = PredictionGlossModel("logos", "word", "A word, statement, or account."),
                    ),
                    WordPredictionModel(
                        text = "esti",
                        probabilities = mapOf("esti" to 0.87),
                        box = PredictionBoxModel(240.0, 60.0, 120.0, 180.0),
                        line = 0,
                        gloss = PredictionGlossModel("eimi", "is", "Third-person singular of to be."),
                    ),
                ),
                modelVersion = "preview",
            ),
            selectedPredictionIndex = null,
            onPredictionSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PredictionInfoEmptyPreview() {
    MaterialTheme {
        PredictionSummary(
            prediction = PredictionModel(predictions = emptyList(), modelVersion = "preview"),
            selectedPredictionIndex = null,
            onPredictionSelected = {},
        )
    }
}
