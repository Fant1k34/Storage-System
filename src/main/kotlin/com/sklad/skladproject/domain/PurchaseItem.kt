package com.sklad.skladproject.domain

data class PurchaseItem(
    val listingItem: String,
    val quantity: Quantity,
    val pack: Package,
    val storageName: String,
    val boughtPrice: Quantity,
    val soldPrice: Quantity
)
