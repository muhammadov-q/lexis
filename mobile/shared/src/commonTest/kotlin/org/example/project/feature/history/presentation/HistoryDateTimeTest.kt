package org.example.project.feature.history.presentation

import org.example.project.common.presentation.helpers.HistoryDateTimeFormatter
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class HistoryDateTimeTest {
    @Test
    fun formatsUtcTimestampInRequestedLocalTimeZone() {
        assertEquals(
            "2026-01-15 12:30",
            HistoryDateTimeFormatter.format("2026-01-15T10:30:45Z", TimeZone.of("Europe/Athens")),
        )
    }

    @Test
    fun preservesFallbackForInvalidTimestamp() {
        assertEquals(
            "not-a-date",
            HistoryDateTimeFormatter.format("not-a-date", TimeZone.of("Europe/Athens")),
        )
    }
}
