package com.sklad.skladproject.repository.database.storage

import com.sklad.skladproject.repository.database.storage.tables.ItemTable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DatabaseItemRepository(
    val itemTable: ItemTable,
) {
    val logger = LoggerFactory.getLogger("DatabaseItemRepository")

    fun trySaveItem(item: String): Result<Long> {
        if (item.isBlank()) {
            logger.error("Item name must not be blank")
            return Result.failure(Throwable("Item name must not be blank"))
        }

        return itemTable.trySaveListingItemAndReturnId(item)
    }
}
