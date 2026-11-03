package org.example.project.feature.camera.presentation.composables.predictions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing
import org.example.project.common.presentation.ui.theme.ScriptLensTypography
import org.example.project.feature.camera.presentation.models.PredictionGlossModel
import org.example.project.feature.camera.presentation.models.PredictionBoxModel
import org.example.project.feature.camera.presentation.models.PredictionModel
import org.example.project.feature.camera.presentation.models.WordPredictionModel
import org.jetbrains.compose.resources.stringResource
import scriptlens.shared.generated.resources.Res
import scriptlens.shared.generated.resources.prediction_definition_label
import scriptlens.shared.generated.resources.prediction_empty_message
import scriptlens.shared.generated.resources.prediction_lemma_label
import scriptlens.shared.generated.resources.prediction_recognized_text_label
import scriptlens.shared.generated.resources.prediction_select_word_hint
import scriptlens.shared.generated.resources.prediction_translation_label
import scriptlens.shared.generated.resources.prediction_transcription_label
import scriptlens.shared.generated.resources.prediction_gloss_unavailable
import org.example.project.common.presentation.ui.theme.ScriptLensColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PredictionSummary(
    prediction: PredictionModel?,
    selectedPredictionIndex: Int?,
    onPredictionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    prediction?.let {
        val groupedWords = prediction.predictions.withIndex().groupBy(
            keySelector = { indexedWord -> indexedWord.value.line },
            valueTransform = { indexedWord -> indexedWord },
        )
        val selectedPrediction = selectedPredictionIndex
            ?.takeIf { index -> index in prediction.predictions.indices }
            ?.let { prediction.predictions[it] }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(ScriptLensColors.Surface)
                .padding(ScriptLensSpacing.padding_16)
        ) {
            Text(
                modifier = Modifier.padding(bottom = ScriptLensSpacing.padding_8),
                text = stringResource(Res.string.prediction_transcription_label),
                color = ScriptLensColors.Subtext
            )

            if (prediction.predictions.isEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(ScriptLensColors.Muted)
                        .padding(ScriptLensSpacing.padding_12),
                    text = stringResource(Res.string.prediction_empty_message),
                    color = ScriptLensColors.Subtext,
                    style = ScriptLensTypography.Body,
                )
                return@Column
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(ScriptLensColors.Background)
                    .border(1.dp, ScriptLensColors.Border, MaterialTheme.shapes.large)
                    .padding(ScriptLensSpacing.padding_12)
                    .heightIn(min = 220.dp, max = 320.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_4),
                verticalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_8),
                maxItemsInEachRow = 1,
            ) {
                groupedWords.entries.forEach { lineEntry ->
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_4),
                        verticalArrangement = Arrangement.spacedBy(ScriptLensSpacing.padding_4),
                        maxItemsInEachRow = Int.MAX_VALUE,
                    ) {
                        lineEntry.value.forEach { indexedWord ->
                            val isSelected = indexedWord.index == selectedPredictionIndex
                            Text(
                                text = indexedWord.value.text,
                                style = if (isSelected) ScriptLensTypography.Subtitle else ScriptLensTypography.Body,
                                color = ScriptLensColors.Text,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ScriptLensColors.Accent.copy(alpha = 0.25f) else ScriptLensColors.Surface)
                                    .clickable { onPredictionSelected(indexedWord.index) }
                                    .padding(horizontal = ScriptLensSpacing.padding_8, vertical = ScriptLensSpacing.padding_4),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ScriptLensSpacing.padding_16)
                    .clip(MaterialTheme.shapes.large)
                    .background(ScriptLensColors.Background)
                    .border(1.dp, ScriptLensColors.Border, MaterialTheme.shapes.large)
                    .padding(ScriptLensSpacing.padding_12)
            ) {
                Text(
                    text = stringResource(Res.string.prediction_translation_label),
                    style = ScriptLensTypography.Label,
                    color = ScriptLensColors.Subtext,
                )

                if (selectedPrediction == null) {
                    Text(
                        modifier = Modifier.padding(top = ScriptLensSpacing.padding_8),
                        text = stringResource(Res.string.prediction_select_word_hint),
                        style = ScriptLensTypography.Body,
                        color = ScriptLensColors.Subtext,
                    )
                } else {
                    val unavailableText = stringResource(Res.string.prediction_gloss_unavailable)
                    Text(
                        text = selectedPrediction.gloss.translation.ifBlank { unavailableText },
                        style = ScriptLensTypography.Heading,
                        color = ScriptLensColors.Text,
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
                }
            }
        }
    }
}

@Composable
private fun PredictionDetailRow(label: String, value: String, unavailableText: String) {
    Text(
        modifier = Modifier.padding(top = ScriptLensSpacing.padding_8),
        text = label,
        style = ScriptLensTypography.Label,
        color = ScriptLensColors.Subtext,
    )
    Text(
        text = value.ifBlank { unavailableText },
        style = ScriptLensTypography.Body,
        color = ScriptLensColors.Text,
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

