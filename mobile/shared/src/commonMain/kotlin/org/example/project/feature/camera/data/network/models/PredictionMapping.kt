package org.example.project.feature.camera.data.network.models

import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.PredictionBox
import org.example.project.feature.camera.domain.model.PredictionGloss
import org.example.project.feature.camera.domain.model.WordPrediction

internal fun mapTo(predictionDto: PredictionDto): Prediction =
    Prediction(
        predictions = predictionDto.predictions.map { mapTo(it) },
        modelVersion = predictionDto.modelVersion,
    )

internal fun mapTo(predictionItemDto: PredictionItemDto): WordPrediction =
    WordPrediction(
        text = predictionItemDto.text,
        probabilities = predictionItemDto.probabilities,
        box = PredictionBox(
            x = predictionItemDto.box.x,
            y = predictionItemDto.box.y,
            w = predictionItemDto.box.w,
            h = predictionItemDto.box.h,
        ),
        line = predictionItemDto.line,
        gloss = PredictionGloss(
            lemma = predictionItemDto.gloss?.lemma.orEmpty(),
            translation = predictionItemDto.gloss?.translation.orEmpty(),
            definition = predictionItemDto.gloss?.definition.orEmpty(),
        ),
        lineTranslation = predictionItemDto.lineTranslation,
    )
