package com.sklad.skladproject.dto

data class PackageRequestDto(
    // Name of the package
    val name: String,
    // Weight of the package
    val weight: Double,
    // Package weight measure unit ID
    val weightUnitId: Long,
)
