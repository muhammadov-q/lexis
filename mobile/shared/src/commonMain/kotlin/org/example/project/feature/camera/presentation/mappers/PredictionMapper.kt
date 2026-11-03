package org.example.project.feature.camera.presentation.mappers

import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.PredictionBox
import org.example.project.feature.camera.domain.model.PredictionGloss
import org.example.project.feature.camera.domain.model.WordPrediction
import org.example.project.feature.camera.presentation.models.PredictionBoxModel
import org.example.project.feature.camera.presentation.models.PredictionGlossModel
import org.example.project.feature.camera.presentation.models.PredictionModel
import org.example.project.feature.camera.presentation.models.WordPredictionModel

fun mapPredictionToUiModel(prediction: Prediction): PredictionModel =
    PredictionModel(
        predictions = prediction.predictions.map(::mapWordPredictionToUiModel),
        modelVersion = prediction.modelVersion,
    )

private fun mapWordPredictionToUiModel(
    prediction: WordPrediction,
): WordPredictionModel =
    WordPredictionModel(
        text = prediction.text,
        probabilities = prediction.probabilities,
        box = mapPredictionBoxToUiModel(prediction.box),
        line = prediction.line,
        gloss = mapPredictionGlossToUiModel(prediction.gloss),
    )

private fun mapPredictionBoxToUiModel(box: PredictionBox): PredictionBoxModel =
    PredictionBoxModel(
        x = box.x,
        y = box.y,
        w = box.w,
        h = box.h,
    )

private fun mapPredictionGlossToUiModel(gloss: PredictionGloss): PredictionGlossModel =
    PredictionGlossModel(
        lemma = gloss.lemma,
        translation = gloss.translation,
        definition = gloss.definition,
    )

