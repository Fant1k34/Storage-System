package com.sklad.skladproject.domain

data class PurchaseItem(
    val date: String,
    val listingItem: String,
    val quantityAmount: Float,
    val quantityMeasureId: Long,
    val packageId: Long,
    val price: Float,
    val currencyId: Long
)
