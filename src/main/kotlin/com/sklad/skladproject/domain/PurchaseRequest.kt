package com.sklad.skladproject.domain

data class PurchaseRequest(
    // Date of the purchase operation
    val date: String,

    // Name of the item
    val itemId: Long,
    // Quantity of the item
    val quantity: Float,
    // Quantity measure unit ID
    val quantityUnitId: Long,

    // Package ID
    val packageId: Long,

    // Bought price of the item
    val boughtPrice: Float,
    // Currency id
    val currency: Long,
)