package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.dto.PurchaseOperationDto
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


    // Stored date may be either "2026-10-25" or "2026-10-25T14:30:00Z", so the day is matched by its first 10 characters
    fun tryGetPurchaseOperationsByDate(date: String, limit: Int, offset: Int): List<PurchaseOperationDto> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement(
                    "SELECT H.id, H.date, " +
                            "H.item_id, ITEM.item_name, " +
                            "H.item_quantity, H.item_unit, ITEM_UNIT.unit_name, " +
                            "H.package_id, PACKAGE.package_name, PACKAGE.package_weight, PACKAGE_UNIT.unit_name, H.package_amount, " +
                            "H.bought_price, H.currency, CURRENCY.currency_name, " +
                            "H.operation_timestamp " +
                            "FROM PURCHASE_OPERATION_HISTORY AS H " +
                            "LEFT JOIN ITEM ON ITEM.id = H.item_id " +
                            "LEFT JOIN UNIT AS ITEM_UNIT ON ITEM_UNIT.id = H.item_unit " +
                            "LEFT JOIN PACKAGE ON PACKAGE.id = H.package_id " +
                            "LEFT JOIN UNIT AS PACKAGE_UNIT ON PACKAGE_UNIT.id = PACKAGE.package_weight_unit_id " +
                            "LEFT JOIN CURRENCY ON CURRENCY.id = H.currency " +
                            "WHERE LEFT(H.date, 10) = ? " +
                            "ORDER BY H.operation_timestamp, H.id " +
                            "LIMIT ? OFFSET ?"
                )
                statement.setString(1, date)
                statement.setInt(2, limit)
                statement.setInt(3, offset)

                val result = mutableListOf<PurchaseOperationDto>()
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    result.add(
                        PurchaseOperationDto(
                            id = resultSet.getLong(1),
                            date = resultSet.getString(2),
                            itemId = resultSet.getLong(3),
                            itemName = resultSet.getString(4),
                            quantity = resultSet.getDouble(5),
                            quantityUnitId = resultSet.getLong(6),
                            quantityUnitName = resultSet.getString(7),
                            packageId = resultSet.getLong(8),
                            packageName = resultSet.getString(9),
                            packageWeight = resultSet.getDouble(10),
                            packageWeightUnitName = resultSet.getString(11),
                            packageAmount = resultSet.getInt(12),
                            boughtPrice = resultSet.getDouble(13),
                            currencyId = resultSet.getLong(14),
                            currencyName = resultSet.getString(15),
                            operationTimestamp = resultSet.getLong(16),
                        )
                    )
                }

                statement.close()
                logger.info("Extracted ${result.size} purchase operations for date $date (limit $limit, offset $offset)")
                return result
            }
        } catch (e: Throwable) {
            logger.error("Error getting purchase operations for date $date (limit $limit, offset $offset)", e)
            return emptyList()
        }
    }
}
