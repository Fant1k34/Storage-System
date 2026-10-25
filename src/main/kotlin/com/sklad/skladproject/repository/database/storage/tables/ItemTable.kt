package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.dto.ItemDto
import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import kotlin.use
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class ItemTable(val databaseAccessRepository: DatabaseAccessRepository) {
    private val logger = LoggerFactory.getLogger("ListingItemStorage")

    fun checkIfItemIdExists(id: Long): Boolean? {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id FROM ITEM WHERE id = ?")
                statement.setLong(1, id)

                val resultSet = statement.executeQuery()
                val exists = resultSet.next()

                statement.close()

                return exists
            }
        } catch (e: Throwable) {
            logger.warn("Error checking if listing_item_id=$id exists in database", e)
        }
        return null
    }

    fun trySaveListingItemAndReturnId(item: String): Result<Long> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO ITEM (item_name) VALUES (?) ON CONFLICT (item_name) DO UPDATE SET item_name = EXCLUDED.item_name RETURNING id")
                statement.setString(1, item)

                val resultSet = statement.executeQuery()
                resultSet.next()

                val operationId = resultSet.getLong(1)
                statement.close()

                logger.info("Item $item is saved to table ITEM in database")
                return Result.success(operationId)
            }
        } catch (e: Throwable) {
            logger.warn("Error saving new item $item\n", e)

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

    fun tryGetAllItems(): List<ItemDto> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id, item_name FROM ITEM ORDER BY id")

                val result = mutableListOf<ItemDto>()
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    result.add(ItemDto(id = resultSet.getLong(1), name = resultSet.getString(2)))
                }

                statement.close()
                return result
            }
        } catch (e: Throwable) {
            logger.warn("Error getting all items from table ITEM", e)
            return emptyList()
        }
    }

    fun tryGetItemByName(name: String): ItemDto? {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id, item_name FROM ITEM WHERE item_name = ?")
                statement.setString(1, name)

                val resultSet = statement.executeQuery()
                val result = if (resultSet.next()) {
                    ItemDto(id = resultSet.getLong(1), name = resultSet.getString(2))
                } else {
                    null
                }

                statement.close()
                return result
            }
        } catch (e: Throwable) {
            logger.warn("Error getting items by name $name from table ITEM", e)
            return null
        }
    }
}
