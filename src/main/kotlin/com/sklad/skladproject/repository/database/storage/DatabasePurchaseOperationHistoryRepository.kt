package com.sklad.skladproject.repository.database.storage

import com.sklad.skladproject.domain.PurchaseItem
import com.sklad.skladproject.repository.database.storage.tables.ListingItemStorage
import com.sklad.skladproject.repository.database.storage.tables.MeasureUnitStorage
import com.sklad.skladproject.repository.database.storage.tables.PurchaseOperationHistoryTable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DatabasePurchaseOperationHistoryRepository(
    val listingItemStorage: ListingItemStorage,
    val measureUnitStorage: MeasureUnitStorage,
    val purchaseOperationHistoryTable: PurchaseOperationHistoryTable
) {
    val logger = LoggerFactory.getLogger("DatabasePurchaseOperationHistoryRepository")

    fun trySavePurchaseOperation(
        purchaseItem: PurchaseItem,
        date: String
    ): Boolean {
        val listingItem = listingItemStorage.tryGetListingItemId(purchaseItem.listingItem) ?: return false

        val purchaseItemQuantity = purchaseItem.quantity.number
        val purchaseItemMeasureId = measureUnitStorage.tryGetMeasureUnitId(purchaseItem.quantity.unit.unitName)
        if (purchaseItemMeasureId == null) {
            logger.error("Measure unit for purchase item ${purchaseItem.quantity.unit.unitName} does not exist in database")
            return false
        }

        val purchaseItemBoughtPrice = purchaseItem.boughtPrice.number
        val purchaseItemBoughtPriceUnitId =
            measureUnitStorage.tryGetMeasureUnitId(purchaseItem.boughtPrice.unit.unitName)
        if (purchaseItemBoughtPriceUnitId == null) {
            logger.error("Measure unit for purchase item ${purchaseItem.boughtPrice.unit.unitName} does not exist in database")
            return false
        }

        purchaseOperationHistoryTable.trySavePurchaseOperation(
            listingItem,
            purchaseItemQuantity,
            purchaseItemMeasureId,
            purchaseItemBoughtPrice,
            purchaseItemBoughtPriceUnitId,
            date
        )
        return true
    }
}