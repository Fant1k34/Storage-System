package com.sklad.skladproject.dto

import io.swagger.v3.oas.annotations.media.Schema

// Purchase operation as stored in history, with referenced names resolved for convenience
data class PurchaseOperationDto(
    val id: Long,
    // Date of the purchase operation, as it was sent by the client (ISO 8601)
    val date: String,

    val itemId: Long,
    val itemName: String,

    val quantity: Double,
    val quantityUnitId: Long,
    val quantityUnitName: String,

    val packageId: Long,
    val packageName: String,
    val packageWeight: Double,
    val packageWeightUnitName: String,

    val boughtPrice: Double,
    val currencyId: Long,
    val currencyName: String,

    // Timestamp of the last state change
    @Schema(description = "Unix time in milliseconds of the last state change", example = "1792003800000")
    val operationTimestamp: Long,
)
