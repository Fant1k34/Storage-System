package com.sklad.skladproject.repository.database.storage.dao

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.use

@Repository
class PurchaseItemStorage(val databaseStorage: DatabaseStorage) {
    private val logger = LoggerFactory.getLogger("PurchaseItemStorage")

    fun trySavePurchase(
        itemId: Int,
        storageId: Int,
        nettItemQuantity: Double,
        nettItemQuantityUnitId: Int,
        boughtPrice: Double,
        boughtPriceUnitId: Int,
        sellPrice: Double,
        sellPriceUnitId: Int
    ): Boolean {
        try {
            databaseStorage.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO ITEM_IN_STORAGE (item_id, storage_id, nett_item_quantity, nett_item_quantity_unit_id, bought_price, bought_price_unit, sell_price, sell_price_unit) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                statement.setInt(1, itemId)
                statement.setInt(2, storageId)
                statement.setDouble(3, nettItemQuantity)
                statement.setInt(4, nettItemQuantityUnitId)
                statement.setDouble(5, boughtPrice)
                statement.setInt(6, boughtPriceUnitId)
                statement.setDouble(7, sellPrice)
                statement.setInt(8, sellPriceUnitId)
                statement.executeUpdate()
                statement.close()

                logger.info("New purchase is saved to ITEM_IN_STORAGE in database")
                return true
            }
        } catch (e: Throwable) {
            logger.warn("Error saving new purchase\n. It may already exist", e)
        }

        return false
    }
}