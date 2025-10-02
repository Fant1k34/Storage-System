package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.use

@Repository
class ListingItemTable(val databaseAccessRepository: DatabaseAccessRepository) {
    private val logger = LoggerFactory.getLogger("ListingItemStorage")

    fun trySaveListingItemAndReturnId(item: String): Result<Long> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO ITEM (item_name) VALUES (?) ON CONFLICT (item_name) DO NOTHING RETURNING id")
                statement.setString(1, item)

                val resultSet = statement.executeQuery()
                resultSet.next()

                val operationId = resultSet.getLong(1)
                statement.close()

                logger.info("Item $item is saved to table ITEM in database")
                return Result.success(operationId)
            }
        } catch (e: Throwable) {
            logger.warn("Error saving new item $item\n. It may already exist", e)

            return Result.failure(Throwable(e))
        }
    }

    fun tryGetListingItemId(itemName: String): Long? {
        logger.info("Getting listing item for name $itemName in ITEM table")
        var itemId: Long

        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id FROM ITEM WHERE item_name = ?")
                statement.setString(1, itemName)

                val resultSet = statement.executeQuery()
                resultSet.next()
                itemId = resultSet.getLong(1)

                statement.close()
            }
        } catch (e: Throwable) {
            logger.warn("Error getting listing item id for name $itemName\n. It does not exist", e)
            return null
        }

        return itemId
    }
}