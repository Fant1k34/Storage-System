package com.sklad.skladproject.dto

import io.swagger.v3.oas.annotations.media.Schema

data class MeasureUnitRequestDto(
    // Name of the measure unit (for example, "kg", "l", "amount")
    @Schema(example = "kg", minLength = 1)
    val name: String,
)
