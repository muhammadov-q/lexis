package org.example.project.common.presentation.helpers

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal object HistoryDateTimeFormatter {
    fun format(raw: String): String = format(raw, TimeZone.currentSystemDefault())

    fun format(raw: String, timeZone: TimeZone): String {
        val normalized = runCatching {
            Instant.parse(raw)
                .toLocalDateTime(timeZone)
                .toString()
                .replace('T', ' ')
        }.getOrElse {
            raw.removeSuffix("Z").replace('T', ' ')
        }
        return if (normalized.length >= 16) normalized.substring(0, 16) else normalized
    }
}
