package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.dto.MeasureUnitDto
import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import kotlin.use
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class MeasureUnitTable(val databaseAccessRepository: DatabaseAccessRepository) {
    private val logger = LoggerFactory.getLogger("MeasureUnitStorage")

    fun trySaveMeasureUnitAndReturnId(unitName: String): Result<Long> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO UNIT (unit_name) VALUES (?) ON CONFLICT (unit_name) DO UPDATE SET unit_name = EXCLUDED.unit_name RETURNING id")
                statement.setString(1, unitName)

                val resultSet = statement.executeQuery()
                resultSet.next()

                val unitId = resultSet.getLong(1)
                statement.close()

                logger.info("Measure unit id for name $unitName is saved to table UNIT in database")
                return Result.success(unitId)
            }
        } catch (e: Throwable) {
            logger.warn("Error saving new measure unit id for name $unitName\n. It may already exist", e)

            return Result.failure(Throwable(e))
        }
    }

    fun checkIfMeasureUnitId(id: Long): Boolean? {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id FROM UNIT WHERE id = ?")
                statement.setLong(1, id)

                val resultSet = statement.executeQuery()
                resultSet.next()
                val result = resultSet.fetchSize

                statement.close()

                return result == 1
            }
        } catch (e: Throwable) {
            logger.warn("Error checking if measure_unit_id=$id exists in database", e)
        }
        return null
    }

    fun tryGetMeasureUnitId(unitName: String): Long? {
        logger.info("Getting measure unit id for name $unitName")
        var packUnitId: Long

        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id FROM UNIT WHERE unit_name = ?")
                statement.setString(1, unitName)

                val resultSet = statement.executeQuery()
                resultSet.next()
                packUnitId = resultSet.getLong(1)

                statement.close()
            }
        } catch (e: Throwable) {
            logger.warn("Error getting measure unit id for name $unitName\n. It does not exist", e)
            return null
        }

        return packUnitId
    }

    fun tryGetAllMeasureUnits(): List<MeasureUnitDto> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id, unit_name FROM UNIT ORDER BY id")

                val result = mutableListOf<MeasureUnitDto>()
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    result.add(MeasureUnitDto(id = resultSet.getLong(1), name = resultSet.getString(2)))
                }

                statement.close()
                return result
            }
        } catch (e: Throwable) {
            logger.warn("Error getting all measure units from table UNIT", e)
            return emptyList()
        }
    }

    fun tryGetMeasureUnitByName(name: String): MeasureUnitDto? {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id, unit_name FROM UNIT WHERE unit_name = ?")
                statement.setString(1, name)

                val resultSet = statement.executeQuery()
                val result = if (resultSet.next()) {
                    MeasureUnitDto(id = resultSet.getLong(1), name = resultSet.getString(2))
                } else {
                    null
                }

                statement.close()
                return result
            }
        } catch (e: Throwable) {
            logger.warn("Error getting measure units by name $name from table UNIT", e)
            return null
        }
    }
}
