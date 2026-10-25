package com.sklad.skladproject.dto

import io.swagger.v3.oas.annotations.media.Schema

data class PurchaseRequestDto(
    // Date of the purchase operation
    @Schema(description = "ISO 8601: a date (`2026-10-25`) or a date-time, optionally with offset (`2026-10-25T14:30:00Z`)", example = "2026-10-25")
    val date: String,

    // Name of the item
    @Schema(description = "Id of an existing item", example = "1")
    val itemId: Long,
    // Quantity of the item
    @Schema(description = "Quantity of the item, must be positive", example = "2.5", exclusiveMinimum = true, minimum = "0")
    val quantity: Float,
    // Quantity measure unit ID
    @Schema(description = "Id of an existing measure unit", example = "1")
    val quantityUnitId: Long,

    // Package ID
    @Schema(description = "Id of an existing package", example = "1")
    val packageId: Long,
    // Number of packages
    @Schema(description = "Number of packages, must be positive", example = "3", minimum = "1")
    val packageAmount: Int,

    // Bought price of the item
    @Schema(description = "Bought price, must be positive", example = "100", exclusiveMinimum = true, minimum = "0")
    val boughtPrice: Float,
    // Currency id
    @Schema(description = "Id of an existing currency", example = "1")
    val currency: Long,
)
