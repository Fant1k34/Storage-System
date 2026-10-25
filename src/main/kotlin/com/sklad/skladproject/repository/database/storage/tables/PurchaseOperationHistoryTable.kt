package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PurchaseOperationHistoryTable(val databaseAccessRepository: DatabaseAccessRepository) {
    val logger = LoggerFactory.getLogger("PurchaseOperationHistoryTable")

    fun savePurchaseOperationAndReturnId(
        date: String,
        listingItemId: Long,
        itemQuantityNumber: Float,
        itemQuantityUnitId: Long,
        packageAmount: Int,
        packageId: Long,
        boughtPrice: Float,
        boughtPriceUnitId: Long,
        lastStateChangedTimestamp: Long
    ): Result<Long> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO PURCHASE_OPERATION_HISTORY (item_id, item_quantity, item_unit, package_amount, package_id, bought_price, currency, date, operation_timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id")
                statement.setLong(1, listingItemId)
                statement.setFloat(2, itemQuantityNumber)
                statement.setLong(3, itemQuantityUnitId)
                statement.setInt(4, packageAmount)
                statement.setLong(5, packageId)
                statement.setFloat(6, boughtPrice)
                statement.setLong(7, boughtPriceUnitId)
                statement.setString(8, date)
                statement.setLong(9, lastStateChangedTimestamp)

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