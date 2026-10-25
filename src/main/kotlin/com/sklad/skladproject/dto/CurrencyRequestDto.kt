package com.sklad.skladproject.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CurrencyRequestDto(
    // Name of the currency
    @Schema(example = "rub", minLength = 1)
    val name: String,
)
