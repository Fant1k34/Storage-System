package com.sklad.skladproject.repository.database.storage

import com.sklad.skladproject.dto.PurchaseOperationDto
import com.sklad.skladproject.domain.PurchaseRequest
import com.sklad.skladproject.repository.database.storage.tables.ItemTable
import com.sklad.skladproject.repository.database.storage.tables.MeasureUnitTable
import com.sklad.skladproject.repository.database.storage.tables.PackageTable
import com.sklad.skladproject.repository.database.storage.tables.PurchaseOperationHistoryTable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DatabasePurchaseOperationHistoryRepository(
    val itemTable: ItemTable,
    val measureUnitTable: MeasureUnitTable,
    val purchaseOperationHistoryTable: PurchaseOperationHistoryTable,
    val packageTable: PackageTable,
) {
    val logger = LoggerFactory.getLogger("DatabasePurchaseOperationHistoryRepository")

    fun trySavePurchase(purchaseRequest: PurchaseRequest): Result<Long> {
        val itemIdExists = itemTable.checkIfItemIdExists(purchaseRequest.itemId)
        if (itemIdExists == null) {
            logger.error("Item with id ${purchaseRequest.itemId} does not exist in database")
            return Result.failure(Throwable("Item does not exist in database"))
        }

        val measureIdExists = measureUnitTable.checkIfMeasureUnitId(purchaseRequest.quantityUnitId)
        if (measureIdExists == null) {
            logger.error("Measure unit with id ${purchaseRequest.quantityUnitId} does not exist in database")
            return Result.failure(Throwable("Measure unit does not exist in database"))
        }

        val packageIdExists = packageTable.checkIfPackageIdExists(purchaseRequest.packageId)
        if (packageIdExists == null) {
            logger.error("Package with id ${purchaseRequest.packageId} does not exist in database")
            return Result.failure(Throwable("Package does not exist in database"))
        }

        val currencyIdExists = measureUnitTable.checkIfMeasureUnitId(purchaseRequest.currency)
        if (currencyIdExists == null) {
            logger.error("Currency with id ${purchaseRequest.currency} does not exist in database")
            return Result.failure(Throwable("Currency does not exist in database"))
        }

        val currentTimestamp = System.currentTimeMillis()

        val purchaseRequestId = purchaseOperationHistoryTable.savePurchaseOperationAndReturnId(
            date = purchaseRequest.date,
            listingItemId = purchaseRequest.itemId,
            itemQuantityNumber = purchaseRequest.quantity,
            itemQuantityUnitId = purchaseRequest.quantityUnitId,
            packageId = purchaseRequest.packageId,
            boughtPrice = purchaseRequest.boughtPrice,
            boughtPriceUnitId = purchaseRequest.currency,
            lastStateChangedTimestamp = currentTimestamp
        )
        return purchaseRequestId
    }


    fun getPurchaseOperationsByDate(date: String, limit: Int, offset: Int): List<PurchaseOperationDto> =
        purchaseOperationHistoryTable.tryGetPurchaseOperationsByDate(date, limit, offset)
}
