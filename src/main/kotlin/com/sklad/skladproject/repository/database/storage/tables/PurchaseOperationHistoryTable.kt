package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PurchaseOperationHistoryTable(val databaseAccessRepository: DatabaseAccessRepository) {
    val logger = LoggerFactory.getLogger("PurchaseOperationHistoryTable")

    fun trySavePurchaseOperationAndReturnId(
        date: String,
        listingItemId: Long,
        itemQuantityNumber: Double,
        itemQuantityUnitId: Long,
        boughtPrice: Double,
        boughtPriceUnitId: Long,
        lastStateChangedTimestamp: Long
    ): Result<Long> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO PURCHASE_OPERATION_HISTORY (item_id, item_quantity, item_unit, bought_price, bought_price_unit, date, operation_timestamp) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id")
                statement.setLong(1, listingItemId)
                statement.setDouble(2, itemQuantityNumber)
                statement.setLong(3, itemQuantityUnitId)
                statement.setDouble(4, boughtPrice)
                statement.setLong(5, boughtPriceUnitId)
                statement.setString(6, date)
                statement.setLong(7, lastStateChangedTimestamp)

                val resultSet = statement.executeQuery()
                resultSet.next()

                val operationId = resultSet.getLong(1)
                statement.close()

                logger.info("New purchase operation is saved to PURCHASE_OPERATION_HISTORY in database")
                return Result.success(operationId)
            }
        } catch (e: Throwable) {
            logger.error("Error saving new purchase operation\n", e)
            return Result.failure(Throwable(e))
        }
    }
}