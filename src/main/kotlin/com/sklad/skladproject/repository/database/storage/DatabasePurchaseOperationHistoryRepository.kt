package com.sklad.skladproject.repository.database.storage

import com.sklad.skladproject.domain.PurchaseItem
import com.sklad.skladproject.repository.database.storage.tables.ListingItemTable
import com.sklad.skladproject.repository.database.storage.tables.MeasureUnitTable
import com.sklad.skladproject.repository.database.storage.tables.PurchaseOperationHistoryTable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DatabasePurchaseOperationHistoryRepository(
    val listingItemTable: ListingItemTable,
    val measureUnitTable: MeasureUnitTable,
    val purchaseOperationHistoryTable: PurchaseOperationHistoryTable
) {
    val logger = LoggerFactory.getLogger("DatabasePurchaseOperationHistoryRepository")

    fun trySavePurchaseOperation(purchaseItem: PurchaseItem): Result<Long> {
        val date = purchaseItem.date

        val listingItemSavingResult = listingItemTable.trySaveListingItemAndReturnId(purchaseItem.listingItem)
        val listingItemId =
            listingItemSavingResult.getOrNull() ?: listingItemTable.tryGetListingItemId(purchaseItem.listingItem)
        if (listingItemId == null) {
            logger.error("Listing item ${purchaseItem.listingItem} does not exist in database, despite it was recently added")
            return Result.failure(Throwable("Listing item does not exist in database"))
        }

        val purchaseItemQuantity = purchaseItem.quantity.number
        val purchaseItemMeasureId = measureUnitTable.tryGetMeasureUnitId(purchaseItem.quantity.unit.unitName)
        if (purchaseItemMeasureId == null) {
            logger.error("Measure unit for purchase item ${purchaseItem.quantity.unit.unitName} does not exist in database")
            return Result.failure(Throwable("Measure unit for purchase item does not exist in database"))
        }

        val purchaseItemBoughtPrice = purchaseItem.boughtPrice.number
        val purchaseItemBoughtPriceUnitId = measureUnitTable.tryGetMeasureUnitId(purchaseItem.boughtPrice.unit.unitName)
        if (purchaseItemBoughtPriceUnitId == null) {
            logger.error("Measure unit for purchase item ${purchaseItem.boughtPrice.unit.unitName} does not exist in database")
            return Result.failure(Throwable("Measure unit for purchase bought price does not exist in database"))
        }
        val lastStateChangedTimestamp = purchaseItem.lastStateChangedTimestamp

        val result = purchaseOperationHistoryTable.trySavePurchaseOperationAndReturnId(
            date,
            listingItemId,
            purchaseItemQuantity,
            purchaseItemMeasureId,
            purchaseItemBoughtPrice,
            purchaseItemBoughtPriceUnitId,
            lastStateChangedTimestamp
        )
        return result
    }
}