package com.sklad.skladproject.repository.database.storage.dao

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.use

@Repository
class ListingItemStorage(val databaseStorage: DatabaseStorage) {
    private val logger = LoggerFactory.getLogger("ListingItemStorage")

    fun trySaveListingItem(item: String): Boolean {
        try {
            databaseStorage.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO ITEM (item_name) VALUES (?) ON CONFLICT (item_name) DO NOTHING")
                statement.setString(1, item)
                statement.executeUpdate()
                statement.close()

                logger.info("Item $item is saved to table ITEM in database")
                return true
            }
        } catch (e: Throwable) {
            logger.warn("Error saving new item $item\n. It may already exist", e)
        }

        return false
    }

    fun tryGetListingItemId(itemName: String): Int? {
        logger.info("Getting listing item for name $itemName in ITEM table")
        var itemId: Int

        try {
            databaseStorage.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id FROM ITEM WHERE item_name = ?")
                statement.setString(1, itemName)

                val resultSet = statement.executeQuery()
                resultSet.next()
                itemId = resultSet.getInt(1)

                statement.close()
            }
        } catch (e: Throwable) {
            logger.warn("Error getting listing item id for name $itemName\n. It does not exist", e)
            return null
        }

        return itemId
    }
}