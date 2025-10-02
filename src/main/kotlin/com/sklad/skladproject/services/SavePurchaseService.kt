package com.sklad.skladproject.services

import com.sklad.skladproject.domain.PurchaseItem
import com.sklad.skladproject.repository.database.storage.DatabasePurchaseOperationHistoryRepository
//import com.sklad.skladproject.repository.database.storage.DatabasePurchaseSaverRepository
import org.springframework.stereotype.Service

@Service
class SavePurchaseService(
//    val databasePurchaseSaverRepository: DatabasePurchaseSaverRepository,
    val databasePurchaseOperationHistoryRepository: DatabasePurchaseOperationHistoryRepository
) {
    fun savePurchase(purchaseItem: PurchaseItem) {
//            databasePurchaseSaverRepository.trySavePurchaseItem(item)
        databasePurchaseOperationHistoryRepository.trySavePurchaseOperation(purchaseItem)
    }
}