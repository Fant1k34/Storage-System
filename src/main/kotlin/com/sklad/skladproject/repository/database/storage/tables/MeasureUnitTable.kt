package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.use

@Repository
class MeasureUnitTable(val databaseAccessRepository: DatabaseAccessRepository) {
    private val logger = LoggerFactory.getLogger("MeasureUnitStorage")

    fun tryGetMeasureUnitId(unitName: String): Int? {
        logger.info("Getting measure unit id for name $unitName")
        var packUnitId: Int

        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id FROM UNIT WHERE unit_name = ?")
                statement.setString(1, unitName)

                val resultSet = statement.executeQuery()
                resultSet.next()
                packUnitId = resultSet.getInt(1)

                statement.close()
            }
        } catch (e: Throwable) {
            logger.warn("Error getting measure unit id for name $unitName\n. It does not exist", e)
            return null
        }

        return packUnitId
    }

    fun trySaveMeasureUnit(unitName: String): Boolean {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO UNIT (unit_name) VALUES (?) ON CONFLICT (unit_name) DO NOTHING")
                statement.setString(1, unitName)
                statement.executeUpdate()
                statement.close()

                logger.info("Measure unit id for name $unitName is saved to table UNIT in database")
                return true
            }
        } catch (e: Throwable) {
            logger.warn("Error saving new measure unit id for name $unitName\n. It may already exist", e)
        }

        return false
    }
}