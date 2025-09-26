package com.sklad.skladproject.dto

data class PurchaseItemDto(
    val name: String,
    val storage: String,
    val amount: Double,
    val amountMeasureUnit: String,
    val packName: String?,
    val packWeight: Double?,
    val packMeasureUnit: String,
    val boughtPrice: Double,
    val boughtPriceMeasureUnit: String,
    val soldPrice: Double?,
    val soldPriceMeasureUnit: String?
)
