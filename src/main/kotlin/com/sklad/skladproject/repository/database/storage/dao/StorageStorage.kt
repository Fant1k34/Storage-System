package com.sklad.skladproject.repository.database.storage.dao

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class StorageStorage(val databaseStorage: DatabaseStorage) {
    private val logger = LoggerFactory.getLogger("StorageStorage")

    fun trySaveStorage(storageName: String, storageAddress: String): Boolean {
        try {
            databaseStorage.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO STORAGE (storage_name, storage_address) VALUES (?, ?) ON CONFLICT (storage_name) DO NOTHING")
                statement.executeUpdate()
                statement.close()

                logger.info("New storage is saved to STORAGE table in database")
                return true
            }
        } catch (e: Throwable) {
            logger.warn(
                "Error saving new storage with name $storageName and address $storageAddress. It may already exist",
                e
            )
        }

        return false
    }

    fun tryGetStorageId(storageName: String): Int? {
        logger.info("Getting storage id for name $storageName in STORAGE table")
        var storageId: Int

        try {
            databaseStorage.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id FROM STORAGE WHERE storage_name = ?")
                statement.setString(1, storageName)
                val resultSet = statement.executeQuery()
                resultSet.next()
                storageId = resultSet.getInt(1)

                statement.close()
                return storageId
            }
        } catch (e: Throwable) {
            logger.warn("Storage with name $storageName does not exist in database. Returning null", e)
            return null
        }
    }
}