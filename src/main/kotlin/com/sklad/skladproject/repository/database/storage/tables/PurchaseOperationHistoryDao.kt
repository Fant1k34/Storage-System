package com.sklad.skladproject.repository.database.storage.tables

data class PurchaseOperationHistoryDao(
    val id: Long,
    val itemId: Long,
    val itemName: String,
    val itemQuantity: Double,
    val itemUnitName: String,
    val packageAmount: Int,
    val packageName: String,
    val packageWeight: Double,
    val packageWeightUnitName: String,
    val boughtPrice: Double,
    val boughtPriceUnitName: String,
    val date: String,
    val operationTimestamp: Long
)