package com.sklad.skladproject.services

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Iso8601DateValidatorTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "2026-10-25",
            "2026-10-25T14:30:00",
            "2026-10-25T14:30:00Z",
            "2026-10-25T14:30:00+03:00",
            "2026-10-25T14:30:00.123Z",
            "2024-02-29",
        ]
    )
    fun `accepts ISO 8601 dates`(date: String) {
        assertTrue(Iso8601DateValidator.isValid(date), "expected '$date' to be valid")
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "   ",
            "25.10.2026",
            "10/25/2026",
            "2026-10-25 14:30:00",
            "2026-13-01",
            "2026-02-30",
            "2023-02-29",
            "20261025",
            "yesterday",
        ]
    )
    fun `rejects non ISO 8601 or impossible dates`(date: String) {
        assertFalse(Iso8601DateValidator.isValid(date), "expected '$date' to be invalid")
    }
}
