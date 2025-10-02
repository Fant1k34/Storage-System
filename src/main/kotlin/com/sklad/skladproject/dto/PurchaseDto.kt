package com.sklad.skladproject.dto

// Purchase operation which includes items were bought from a stock
data class PurchaseDto(val items: List<PurchaseItemDto>)
