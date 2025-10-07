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
        packageAmount: Int,
        packageId: Long,
        boughtPrice: Double,
        boughtPriceUnitId: Long,
        lastStateChangedTimestamp: Long
    ): Result<Long> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO PURCHASE_OPERATION_HISTORY (item_id, item_quantity, item_unit, package_amount, package_id, bought_price, bought_price_unit, date, operation_timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id")
                statement.setLong(1, listingItemId)
                statement.setDouble(2, itemQuantityNumber)
                statement.setLong(3, itemQuantityUnitId)
                statement.setInt(4, packageAmount)
                statement.setLong(5, packageId)
                statement.setDouble(6, boughtPrice)
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

    fun tryGetPurchaseOperations(limit: Int, offset: Int): List<PurchaseOperationHistoryDao> {
        databaseAccessRepository.getDataSource().connection.use { connection ->
            val statement =
                connection.prepareStatement("SELECT PURCHASE_OPERATION_HISTORY.id, item_id, item_name, item_quantity, ITEM_UNIT.unit_name, package_amount, PACKAGE.package_name, PACKAGE.package_weight, PACKAGE_UNIT.unit_name, bought_price, CURRENCY.unit_name, date, operation_timestamp FROM PURCHASE_OPERATION_HISTORY LEFT JOIN UNIT AS ITEM_UNIT ON ITEM_UNIT.id=PURCHASE_OPERATION_HISTORY.item_unit LEFT JOIN ITEM on ITEM.id=PURCHASE_OPERATION_HISTORY.item_id LEFT JOIN UNIT AS CURRENCY ON CURRENCY.id=PURCHASE_OPERATION_HISTORY.bought_price_unit LEFT JOIN PACKAGE ON PACKAGE.id=PURCHASE_OPERATION_HISTORY.package_id LEFT JOIN UNIT AS PACKAGE_UNIT ON PACKAGE_UNIT.id=PACKAGE.package_weight_unit_id LIMIT ? OFFSET ?")
            statement.setInt(1, limit)
            statement.setInt(2, offset)

            val listOfPurchaseOperations = mutableListOf<PurchaseOperationHistoryDao>()
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val id = resultSet.getLong(1)
                val itemId = resultSet.getLong(2)
                val itemName = resultSet.getString(3)
                val itemQuantity = resultSet.getDouble(4)
                val itemUnitName = resultSet.getString(5)
                val packageAmount = resultSet.getInt(6)
                val packageName = resultSet.getString(7)
                val packageWeight = resultSet.getDouble(8)
                val packageWeightUnitName = resultSet.getString(9)
                val boughtPrice = resultSet.getDouble(10)
                val boughtPriceUnitName = resultSet.getString(11)
                val date = resultSet.getString(12)
                val operationTimestamp = resultSet.getLong(13)

                val purchaseOperationHistoryDao = PurchaseOperationHistoryDao(
                    id,
                    itemId,
                    itemName,
                    itemQuantity,
                    itemUnitName,
                    packageAmount,
                    packageName,
                    packageWeight,
                    packageWeightUnitName,
                    boughtPrice,
                    boughtPriceUnitName,
                    date,
                    operationTimestamp
                )
                listOfPurchaseOperations.add(purchaseOperationHistoryDao)

                logger.info("Purchase operation extracted with id: $id, item id: $itemId, item name: $itemName, item quantity: $itemQuantity, item unit name: $itemUnitName, bought price: $boughtPrice, bought price unit name: $boughtPriceUnitName, date: $date, operation timestamp: $operationTimestamp")
            }

            return listOfPurchaseOperations
        }
    }
}