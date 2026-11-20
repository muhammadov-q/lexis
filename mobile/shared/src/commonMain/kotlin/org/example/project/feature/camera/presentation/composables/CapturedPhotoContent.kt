package org.example.project.feature.camera.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.common.presentation.ui.composables.buttons.LexisButton
import org.example.project.common.presentation.ui.composables.buttons.LexisButtonStyle
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.composables.pagescan.PageScan
import org.example.project.feature.camera.presentation.helpers.decodePhoto
import org.example.project.common.presentation.models.PredictionBoxModel
import org.example.project.common.presentation.models.PredictionGlossModel
import org.example.project.common.presentation.models.PredictionModel
import org.example.project.common.presentation.models.WordPredictionModel
import org.jetbrains.compose.resources.stringResource
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.action_choose_another_photo
import lexis.shared.generated.resources.action_read_page
import lexis.shared.generated.resources.action_take_new_photo
import lexis.shared.generated.resources.captured_photo_description
import org.example.project.common.presentation.ui.composables.transcript.TranscriptCard
import org.example.project.common.presentation.ui.composables.worddetails.WordDetailsCard

@Composable
fun CapturedPhotoContent(
    photo: ByteArray?,
    prediction: PredictionModel?,
    selectedPredictionIndex: Int?,
    onPredictionSelected: (Int?) -> Unit,
    onTakeNewPhotoClicked: () -> Unit,
    onChooseFromPhotosClicked: () -> Unit,
    modifier: Modifier = Modifier,
    readDurationMs: Long? = null,
    isReading: Boolean = false,
    isAuthenticated: Boolean = false,
    onReadPageClicked: () -> Unit = {},
    onOpenSaved: (Long) -> Unit = {},
    onSignInRequested: () -> Unit = {},
) {
    val image = photo?.let { remember(photo) { decodePhoto(photo) } }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_16),
    ) {
        image?.let {
            PageScan(
                image = it,
                contentDescription = stringResource(Res.string.captured_photo_description),
                prediction = prediction,
                selectedPredictionIndex = selectedPredictionIndex,
                onPredictionSelected = onPredictionSelected,
            )
        }

        if (prediction != null) {
            WordDetailsCard(
                prediction = prediction,
                selectedPredictionIndex = selectedPredictionIndex,
                onPredictionSelected = onPredictionSelected,
                image = image,
                durationMs = readDurationMs,
                footer = {
                    SavedResultLine(
                        savedId = prediction.savedId,
                        isAuthenticated = isAuthenticated,
                        onOpenSaved = onOpenSaved,
                        onSignInRequested = onSignInRequested,
                    )
                },
            )

            TranscriptCard(
                words = prediction.predictions,
                selectedPredictionIndex = selectedPredictionIndex,
                onPredictionSelected = onPredictionSelected,
            )
        } else if (!isReading) {
            LexisButton(
                text = stringResource(Res.string.action_read_page),
                onClick = onReadPageClicked,
                buttonStyle = LexisButtonStyle.Primary,
            )
        }

        Column(modifier = Modifier.padding(top = LexisSpacing.padding_8)) {
            LexisButton(
                modifier = Modifier.padding(bottom = LexisSpacing.padding_8),
                text = stringResource(Res.string.action_take_new_photo),
                onClick = onTakeNewPhotoClicked,
                buttonStyle = LexisButtonStyle.Primary,
            )

            LexisButton(
                modifier = Modifier.padding(bottom = LexisSpacing.padding_4),
                text = stringResource(Res.string.action_choose_another_photo),
                onClick = onChooseFromPhotosClicked,
                buttonStyle = LexisButtonStyle.Secondary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CapturedPhotoPreview() {
    MaterialTheme {
        CapturedPhotoContent(
            photo = null,
            prediction = PredictionModel(
                predictions = listOf(
                    WordPredictionModel(
                        text = "logos",
                        probabilities = mapOf("logos" to 0.92),
                        box = PredictionBoxModel(80.0, 60.0, 120.0, 180.0),
                        line = 0,
                        gloss = PredictionGlossModel(
                            lemma = "logos",
                            translation = "word",
                            definition = "A word or account.",
                        ),
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
                        ),
                    ),
                ),
                modelVersion = "preview",
            ),
            selectedPredictionIndex = 0,
            onPredictionSelected = {},
            onTakeNewPhotoClicked = {},
            onChooseFromPhotosClicked = {},
        )
    }
}
