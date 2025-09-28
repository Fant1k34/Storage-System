package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.use

@Repository
class PurchaseItemTable(val databaseAccessRepository: DatabaseAccessRepository) {
    private val logger = LoggerFactory.getLogger("PurchaseItemStorage")

    fun trySavePurchaseAndReturnId(
        itemId: Int,
        storageId: Int,
        nettItemQuantity: Double,
        nettItemQuantityUnitId: Int,
        sellPrice: Double,
        sellPriceUnitId: Int
    ): Long? {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO ITEM_IN_STORAGE (item_id, storage_id, nett_item_quantity, nett_item_quantity_unit_id, sell_price, sell_price_unit) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (item_id, storage_id, nett_item_quantity_unit_id, sell_price, sell_price_unit) DO UPDATE SET nett_item_quantity = EXCLUDED.nett_item_quantity + ITEM_IN_STORAGE.nett_item_quantity RETURNING id")
                statement.setInt(1, itemId)
                statement.setInt(2, storageId)
                statement.setDouble(3, nettItemQuantity)
                statement.setInt(4, nettItemQuantityUnitId)
                statement.setDouble(5, sellPrice)
                statement.setInt(6, sellPriceUnitId)

                val resultSet = statement.executeQuery()
                logger.info("New purchase is saved to ITEM_IN_STORAGE in database")
                // TODO("Пофиксить внизу, код не работает, кидает ошибку!")
                resultSet.next()
                val operationId = resultSet.getLong(1)

                statement.close()
                return operationId
            }
        } catch (e: Throwable) {
            logger.warn("Error saving new purchase\n. It may already exist", e)
        }

        return null
    }
}