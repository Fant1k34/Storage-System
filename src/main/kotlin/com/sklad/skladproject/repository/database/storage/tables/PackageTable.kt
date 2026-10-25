package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.dto.PackageDto
import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import kotlin.use
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PackageTable(val databaseAccessRepository: DatabaseAccessRepository) {
    private val logger = LoggerFactory.getLogger("PackageStorage")

    fun trySavePackage(name: String, weight: Double, unitId: Int): Boolean? {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO PACKAGE (package_name, package_weight, package_weight_unit_id) VALUES (?, ?, ?) ON CONFLICT (package_name, package_weight, package_weight_unit_id) DO NOTHING")
                statement.setString(1, name)
                statement.setDouble(2, weight)
                statement.setInt(3, unitId)
                statement.executeUpdate()
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

    fun trySavePackageAndReturnId(name: String, weight: Double, unitId: Long): Result<Long> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO PACKAGE (package_name, package_weight, package_weight_unit_id) VALUES (?, ?, ?) ON CONFLICT (package_name, package_weight, package_weight_unit_id) DO UPDATE SET package_name = EXCLUDED.package_name RETURNING id")
                statement.setString(1, name)
                statement.setDouble(2, weight)
                statement.setLong(3, unitId)

                val resultSet = statement.executeQuery()

                resultSet.next()
                val packageId = resultSet.getLong(1)

                statement.close()

                logger.info("New Package $name with $weight (measure unit id: $unitId) is saved to table PACKAGE in database")
                return Result.success(packageId)
            }
        } catch (e: Throwable) {
            logger.warn(
                "Error saving new package $name with $weight (measure unit id: $unitId)\n",
                e
            )

            return Result.failure(Throwable(e))
        }
    }

    fun checkIfPackageIdExists(id: Long): Boolean? {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id FROM PACKAGE WHERE id = ?")
                statement.setLong(1, id)

                val resultSet = statement.executeQuery()
                val exists = resultSet.next()

                statement.close()

                return exists
            }
        } catch (e: Throwable) {
            logger.warn("Error checking if package_id=$id exists in database", e)
        }
        return null
    }

    fun tryGetPackageId(name: String, weight: Double, unitId: Long): Long? {
        val packageId: Long

        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("SELECT id FROM PACKAGE WHERE package_name = ? AND package_weight = ? AND package_weight_unit_id = ?")
                statement.setString(1, name)
                statement.setDouble(2, weight)
                statement.setLong(3, unitId)

                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    packageId = resultSet.getLong(1)
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

    private val selectPackageWithUnit =
        "SELECT PACKAGE.id, package_name, package_weight, package_weight_unit_id, UNIT.unit_name " +
                "FROM PACKAGE LEFT JOIN UNIT ON UNIT.id = PACKAGE.package_weight_unit_id"

    private fun readPackage(resultSet: java.sql.ResultSet): PackageDto {
        return PackageDto(
            id = resultSet.getLong(1),
            name = resultSet.getString(2),
            weight = resultSet.getDouble(3),
            weightUnitId = resultSet.getLong(4),
            weightUnitName = resultSet.getString(5),
        )
    }

    fun tryGetAllPackages(): List<PackageDto> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("$selectPackageWithUnit ORDER BY PACKAGE.id")

                val result = mutableListOf<PackageDto>()
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    result.add(readPackage(resultSet))
                }

                statement.close()
                return result
            }
        } catch (e: Throwable) {
            logger.warn("Error getting all packages from table PACKAGE", e)
            return emptyList()
        }
    }

    // Package name is not unique by itself (unique key is name + weight + unit), so several rows may match
    fun tryGetPackagesByName(name: String): List<PackageDto> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("$selectPackageWithUnit WHERE package_name = ? ORDER BY PACKAGE.id")
                statement.setString(1, name)

                val result = mutableListOf<PackageDto>()
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    result.add(readPackage(resultSet))
                }

                statement.close()
                return result
            }
        } catch (e: Throwable) {
            logger.warn("Error getting packages by name $name from table PACKAGE", e)
            return emptyList()
        }
    }
}
