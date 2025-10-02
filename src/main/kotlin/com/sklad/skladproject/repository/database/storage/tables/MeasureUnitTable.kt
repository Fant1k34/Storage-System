package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.use

@Repository
class MeasureUnitTable(val databaseAccessRepository: DatabaseAccessRepository) {
    private val logger = LoggerFactory.getLogger("MeasureUnitStorage")

    fun trySaveMeasureUnitAndReturnId(unitName: String): Result<Long> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO UNIT (unit_name) VALUES (?) ON CONFLICT (unit_name) DO NOTHING RETURNING id")
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
}