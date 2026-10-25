package com.sklad.skladproject.dto

import io.swagger.v3.oas.annotations.media.Schema

data class PackageRequestDto(
    // Name of the package
    @Schema(example = "box")
    val name: String,
    // Weight of the package
    @Schema(example = "0.5")
    val weight: Double,
    // Package weight measure unit ID
    @Schema(description = "Id of an existing measure unit", example = "1")
    val weightUnitId: Long,
)
