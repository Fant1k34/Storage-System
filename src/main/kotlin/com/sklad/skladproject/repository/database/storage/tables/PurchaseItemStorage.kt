package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.use

@Repository
class PurchaseItemStorage(val databaseAccessRepository: DatabaseAccessRepository) {
    private val logger = LoggerFactory.getLogger("PurchaseItemStorage")

    fun trySavePurchase(
        itemId: Int,
        storageId: Int,
        nettItemQuantity: Double,
        nettItemQuantityUnitId: Int,
        sellPrice: Double,
        sellPriceUnitId: Int
    ): Boolean {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO ITEM_IN_STORAGE (item_id, storage_id, nett_item_quantity, nett_item_quantity_unit_id, sell_price, sell_price_unit) VALUES (?, ?, ?, ?, ?, ?)")
                statement.setInt(1, itemId)
                statement.setInt(2, storageId)
                statement.setDouble(3, nettItemQuantity)
                statement.setInt(4, nettItemQuantityUnitId)
                statement.setDouble(5, sellPrice)
                statement.setInt(6, sellPriceUnitId)
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