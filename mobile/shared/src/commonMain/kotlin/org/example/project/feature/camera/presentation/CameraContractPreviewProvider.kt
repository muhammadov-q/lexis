package org.example.project.feature.camera.presentation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.example.project.feature.camera.presentation.models.CameraScreenErrorState
import org.example.project.feature.camera.presentation.models.CameraScreenState
import org.example.project.feature.camera.presentation.models.PredictionBoxModel
import org.example.project.feature.camera.presentation.models.PredictionGlossModel
import org.example.project.feature.camera.presentation.models.PredictionModel
import org.example.project.feature.camera.presentation.models.WordPredictionModel

class CameraContractPreviewProvider : PreviewParameterProvider<CameraContract> {
    private val prediction = PredictionModel(
        predictions = listOf("logos", "esti", "bios", "kai").mapIndexed { index, word ->
            WordPredictionModel(
                text = word,
                probabilities = mapOf(word to 0.87),
                box = PredictionBoxModel(x = 0.0, y = 0.0, w = 0.0, h = 0.0),
                line = if (index < 2) 0 else 1,
                gloss = PredictionGlossModel(
                    lemma = word,
                    translation = "translation $word",
                    definition = "Definition for $word",
                ),
            )
        },
        modelVersion = "preview",
    )

    override val values = sequenceOf(
        CameraContract(cameraScreenState = CameraScreenState.NoPhotoCaptured),
        CameraContract(
            cameraScreenState = CameraScreenState.PhotoCaptured,
            prediction = prediction,
            selectedPredictionIndex = 0,
        ),
        CameraContract(
            cameraScreenState = CameraScreenState.Error(CameraScreenErrorState.ServiceOffline),
        ),
    )
}

