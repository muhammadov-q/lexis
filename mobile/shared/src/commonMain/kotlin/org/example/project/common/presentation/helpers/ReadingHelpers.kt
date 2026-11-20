package org.example.project.common.presentation.helpers

import org.example.project.common.presentation.models.Crop
import org.example.project.common.presentation.models.PredictionBoxModel
import org.example.project.common.presentation.models.WordPosition
import org.example.project.common.presentation.models.WordPredictionModel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun positionOf(words: List<WordPredictionModel>, index: Int): WordPosition {
    val line = words[index].line
    val before = words.subList(0, index).count { it.line == line }
    return WordPosition(line = line + 1, word = before + 1)
}

fun WordPredictionModel.confidence(): Double? = probabilities[text]

fun WordPredictionModel.hasGloss(): Boolean =
    gloss.translation.isNotBlank() || gloss.lemma.isNotBlank() || gloss.definition.isNotBlank()

fun List<WordPredictionModel>.isTranslated(): Boolean =
    any { it.hasGloss() || !it.lineTranslation.isNullOrBlank() }

fun lineCenters(words: List<WordPredictionModel>): Map<Int, Double> =
    words.filter { it.box.w > 0 && it.box.h > 0 }
        .groupBy { it.line }
        .mapValues { (_, lineWords) ->
            (lineWords.minOf { it.box.y } + lineWords.maxOf { it.box.y + it.box.h }) / 2
        }

const val LOUPE_ASPECT = 3f

fun loupeCrop(box: PredictionBoxModel, imageWidth: Int, imageHeight: Int): Crop {
    val margin = box.h * 0.6
    var w = box.w + margin * 2
    var h = box.h + margin * 2
    if (w / h < LOUPE_ASPECT) w = h * LOUPE_ASPECT else h = w / LOUPE_ASPECT
    w = min(w, imageWidth.toDouble())
    h = min(h, imageHeight.toDouble())
    return Crop(
        x = (box.x + box.w / 2 - w / 2).coerceIn(0.0, max(0.0, imageWidth - w)),
        y = (box.y + box.h / 2 - h / 2).coerceIn(0.0, max(0.0, imageHeight - h)),
        w = w,
        h = h,
    )
}

fun formatDuration(ms: Long): String =
    if (ms < 1000) "$ms ms" else "${(ms / 100.0).roundToInt() / 10.0} s"

fun formatPercent(fraction: Double): String = "${(fraction * 100).roundToInt()}%"

private val PLACEHOLDER_MODEL_VERSION = Regex("^(mock|eparchos-.+)$")

fun isPlaceholderModel(modelVersion: String): Boolean = PLACEHOLDER_MODEL_VERSION.matches(modelVersion)
