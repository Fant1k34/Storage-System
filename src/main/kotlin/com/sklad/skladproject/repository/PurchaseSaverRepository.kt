package com.sklad.skladproject.repository

import com.sklad.skladproject.domain.PurchaseItem

interface PurchaseSaverRepository {
    fun trySavePurchaseItem(purchaseItem: PurchaseItem): Boolean
}