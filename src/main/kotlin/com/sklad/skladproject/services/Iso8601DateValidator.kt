package com.sklad.skladproject.services

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// Accepts ISO 8601 dates in two shapes:
//   2026-10-25                      (date only)
//   2026-10-25T14:30:00[.sss][Z|+03:00]  (date and time, offset is optional)
// Calendar correctness is checked as well: 2026-02-30 is rejected.
object Iso8601DateValidator {
    const val EXPECTED_FORMAT_DESCRIPTION = "ISO 8601, for example 2026-10-25 or 2026-10-25T14:30:00Z"

    private val acceptedFormats = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ISO_DATE_TIME,
    )

    fun isValid(date: String): Boolean {
        if (date.isBlank()) return false

        return acceptedFormats.any { format ->
            try {
                format.parse(date)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }
    }
}
