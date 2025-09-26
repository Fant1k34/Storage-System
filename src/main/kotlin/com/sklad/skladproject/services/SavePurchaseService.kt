package com.sklad.skladproject.services

import com.sklad.skladproject.domain.Purchase
import com.sklad.skladproject.repository.database.storage.DatabasePurchaseSaverRepository
import org.springframework.stereotype.Service

@Service
class SavePurchaseService(val databasePurchaseSaverRepository: DatabasePurchaseSaverRepository) {
    fun savePurchase(purchase: Purchase) {
        purchase.itemsList.forEach { item ->
            databasePurchaseSaverRepository.trySavePurchaseItem(item)
        }
    }
}