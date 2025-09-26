package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.use

@Repository
class PackageStorage(val databaseAccessRepository: DatabaseAccessRepository) {
    private val logger = LoggerFactory.getLogger("PackageStorage")

    fun trySavePackage(name: String, weight: Double, unitId: Int): Boolean? {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO PACKAGE (package_name, package_weight, package_weight_unit_id) VALUES (?, ?, ?) ON CONFLICT (package_name, package_weight, package_weight_unit_id) DO NOTHING")
                statement.setString(1, name)
                statement.setDouble(2, weight)
                statement.setInt(3, unitId)
                statement.executeQuery()
                statement.close()

                logger.info("New Package $name with $weight (measure unit id: $unitId) is saved to table PACKAGE in database")
                return true
            }
        } catch (e: Throwable) {
            logger.warn(
                "Error saving new package $name with $weight (measure unit id: $unitId)\n. It may already exist",
                e
            )
        }

        return false
    }

    fun tryGetPackageId(name: String, weight: Double, unitId: Int): Int? {
        val packageId: Int

        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("SELECT id FROM PACKAGE WHERE package_name = ? AND package_weight = ? AND package_weight_unit_id = ?")
                statement.setString(1, name)
                statement.setDouble(2, weight)
                statement.setInt(3, unitId)

                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    packageId = resultSet.getInt(1)
                }
            }
        } catch (e: Throwable) {
            logger.warn(
                "Error getting package id for name $name and weight $weight and unit $unitId\n. It does not exist",
                e
            )
            return null
        }

        return packageId
    }
}