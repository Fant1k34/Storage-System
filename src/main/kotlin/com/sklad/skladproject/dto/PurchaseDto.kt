package com.sklad.skladproject.dto

data class PurchaseDto(val items: List<PurchaseItemDto>, val totalPrice: Double, val date: String)
