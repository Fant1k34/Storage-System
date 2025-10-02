package com.sklad.skladproject.domain

data class PurchaseItem(
    val date: String,
    val listingItem: String,
    val quantity: Quantity,
    val pack: Package,
    val boughtPrice: Quantity,
    val state: PurchaseItemState,
    val lastStateChangedTimestamp: Long
)
