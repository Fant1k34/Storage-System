package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PurchaseOperationHistoryTable(val databaseAccessRepository: DatabaseAccessRepository) {
    val logger = LoggerFactory.getLogger("PurchaseOperationHistoryTable")

    fun trySavePurchaseOperation(
        listingItemId: Int,
        itemQuantityNumber: Double,
        itemQuantityUnitId: Int,
        boughtPrice: Double,
        boughtPriceUnitId: Int,
        date: String
    ): Boolean {
        try {
            val currentTimestamp = System.currentTimeMillis()

            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO PURCHASE_OPERATION_HISTORY (item_id, item_quantity, item_unit, bought_price, bought_price_unit, date, operation_timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)")
                statement.setInt(1, listingItemId)
                statement.setDouble(2, itemQuantityNumber)
                statement.setInt(3, itemQuantityUnitId)
                statement.setDouble(4, boughtPrice)
                statement.setInt(5, boughtPriceUnitId)
                statement.setString(6, date)
                statement.setLong(7, currentTimestamp)

                logger.info("New purchase operation is saved to PURCHASE_OPERATION_HISTORY in database")
                return true
            }
        } catch (e: Throwable) {
            logger.error("Error saving new purchase operation\n", e)
        }

        return false
    }
}