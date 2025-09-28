package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PurchaseItemInStoreTable(val databaseAccessRepository: DatabaseAccessRepository) {
    val logger = LoggerFactory.getLogger("PurchaseItemInStoreStorage")

    fun trySavePurchaseItemWithPackage(listingItemId: Long, packageId: Long): Boolean {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO ITEM_IN_STORAGE_IN_PACKAGE (item_in_storage_id, package_id) VALUES (?, ?)")
                statement.setLong(1, listingItemId)
                statement.setLong(2, packageId)
                statement.executeUpdate()
                statement.close()

                logger.info("New purchase item with package is saved to ITEM_IN_STORAGE_IN_PACKAGE in database")
                return true
            }
        } catch (e: Throwable) {
            logger.error("Error saving new purchase item with package", e)
        }

        return false
    }
}