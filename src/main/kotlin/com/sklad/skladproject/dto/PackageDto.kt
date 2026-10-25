package com.sklad.skladproject.dto

data class PackageDto(
    val id: Long,
    val name: String,
    val weight: Double,
    val weightUnitId: Long,
    val weightUnitName: String,
)
