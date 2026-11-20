package org.example.project.feature.camera.data.network.models

import org.example.project.feature.camera.domain.model.Prediction
import org.example.project.feature.camera.domain.model.PredictionBox
import org.example.project.feature.camera.domain.model.PredictionGloss
import org.example.project.feature.camera.domain.model.WordPrediction

fun PredictionDto.toDomain(): Prediction =
    Prediction(predictions = predictions.map { it.toDomain() }, modelVersion = modelVersion)

fun PredictionItemDto.toDomain(): WordPrediction =
    WordPrediction(
        text = text,
        probabilities = probabilities,
        box = PredictionBox(x = box.x, y = box.y, w = box.w, h = box.h),
        line = line,
        gloss = PredictionGloss(
            lemma = gloss?.lemma.orEmpty(),
            translation = gloss?.translation.orEmpty(),
            definition = gloss?.definition.orEmpty(),
        ),
        lineTranslation = lineTranslation,
    )
