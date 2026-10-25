package com.sklad.skladproject.dto

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
    val packageAmount: Int,

    val boughtPrice: Double,
    val currencyId: Long,
    val currencyName: String,

    // Timestamp of the last state change
    val operationTimestamp: Long,
)
