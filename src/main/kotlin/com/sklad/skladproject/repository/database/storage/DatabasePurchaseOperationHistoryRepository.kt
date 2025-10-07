package com.sklad.skladproject.repository.database.storage

import com.sklad.skladproject.domain.PurchaseItem
import com.sklad.skladproject.repository.PurchaseBuilder
import com.sklad.skladproject.repository.database.storage.tables.ListingItemTable
import com.sklad.skladproject.repository.database.storage.tables.MeasureUnitTable
import com.sklad.skladproject.repository.database.storage.tables.PackageTable
import com.sklad.skladproject.repository.database.storage.tables.PurchaseOperationHistoryTable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DatabasePurchaseOperationHistoryRepository(
    val listingItemTable: ListingItemTable,
    val measureUnitTable: MeasureUnitTable,
    val purchaseOperationHistoryTable: PurchaseOperationHistoryTable,
    val packageTable: PackageTable,
    val purchaseBuilder: PurchaseBuilder
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

        val purchaseItemPackageUnitId = measureUnitTable.tryGetMeasureUnitId(purchaseItem.pack.weight.unit.unitName)
        if (purchaseItemPackageUnitId == null) {
            logger.error("Measure unit for package ${purchaseItem.pack.weight.unit.unitName} does not exist in database")
            return Result.failure(Throwable("Measure unit for package does not exist in database"))
        }

        val packageAmount = purchaseItem.packageAmount
        val packageId = packageTable.tryGetPackageId(
            purchaseItem.pack.name,
            purchaseItem.pack.weight.number,
            purchaseItemPackageUnitId
        )
        if (packageId == null) {
            logger.error("Package ${purchaseItem.pack.name} with weight ${purchaseItem.pack.weight.number} does not exist in database")
            return Result.failure(Throwable("Package does not exist in database"))
        }

        // TODO - save packages as well

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
            packageAmount,
            packageId,
            purchaseItemBoughtPrice,
            purchaseItemBoughtPriceUnitId,
            lastStateChangedTimestamp
        )
        return result
    }

    fun tryGetPurchaseOperations(limit: Int, offset: Int): List<PurchaseItem> {
        return purchaseOperationHistoryTable.tryGetPurchaseOperations(limit, offset)
            .mapNotNull { op ->
                val quantity = purchaseBuilder.tryCreateQuantity(op.itemQuantity, op.itemUnitName)
                    ?: return@mapNotNull null.also { logger.error("Cannot create ALREADY saved purchase operation from db. Quantity with name=${op.itemQuantity}, unit=${op.itemUnitName} could not be created") }
                val pack = purchaseBuilder.tryCreatePackage(op.packageName, op.packageWeight, op.packageWeightUnitName)
                    ?: return@mapNotNull null.also { logger.error("Cannot create ALREADY saved purchase operation from db. Package with name=${op.packageName}, weight=${op.packageWeight}, unit=${op.packageWeightUnitName} could not be created") }
                val boughtPrice = purchaseBuilder.tryCreateQuantity(op.boughtPrice, op.boughtPriceUnitName)
                    ?: return@mapNotNull null.also { logger.error("Cannot create ALREADY saved purchase operation from db. Bought price with name=${op.boughtPrice}, unit=${op.boughtPriceUnitName} could not be created") }

                PurchaseItem(op.date, op.itemName, quantity, op.packageAmount, pack, boughtPrice, op.operationTimestamp)
            }
    }
}