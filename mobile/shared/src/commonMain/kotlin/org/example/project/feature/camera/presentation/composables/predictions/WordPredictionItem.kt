package org.example.project.feature.camera.presentation.composables.predictions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.project.common.presentation.ui.theme.ScriptLensSpacing
import org.example.project.feature.camera.presentation.models.PredictionGlossModel
import org.example.project.feature.camera.presentation.models.PredictionBoxModel
import org.example.project.feature.camera.presentation.models.WordPredictionModel
import org.example.project.common.presentation.ui.theme.ScriptLensColors
import org.example.project.common.presentation.ui.theme.ScriptLensTypography

@Composable
fun WordPredictionItem(
    modifier: Modifier = Modifier,
    digitPrediction: WordPredictionModel
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(ScriptLensColors.Background)
            .padding(ScriptLensSpacing.padding_12)
            .size(50.dp),
    ) {
        Text(
            text = digitPrediction.text,
            style = ScriptLensTypography.Heading,
            color = ScriptLensColors.Text
        )
        Text(
            text = "${(digitPrediction.probabilities[digitPrediction.text] ?: 0.0) * 100}%",
            style = ScriptLensTypography.Label,
            color = ScriptLensColors.Border
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PredictionItemPreview() {
    MaterialTheme {
        WordPredictionItem(
            digitPrediction = WordPredictionModel(
                text = "logos",
                probabilities = mapOf("logos" to 0.87),
                box = PredictionBoxModel(
                    x = 0.0,
                    y = 0.0,
                    w = 100.0,
                    h = 100.0,
                ),
                line = 0,
                gloss = PredictionGlossModel(
                    lemma = "logos",
                    translation = "word",
                    definition = "A word or statement.",
                ),
            ),
        )
    }
}


