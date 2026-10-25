package com.sklad.skladproject.dto

import io.swagger.v3.oas.annotations.media.Schema

data class ItemRequestDto(
    // Name of the item
    @Schema(example = "apple", minLength = 1)
    val name: String,
)
