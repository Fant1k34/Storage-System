package com.sklad.skladproject.repository.database.storage.tables

import com.sklad.skladproject.repository.database.storage.DatabaseAccessRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import kotlin.use

@Repository
class CurrencyTable(val databaseAccessRepository: DatabaseAccessRepository) {
    private val logger = LoggerFactory.getLogger("CurrencyStorage")

    fun checkIfCurrencyIdExists(id: Long): Boolean? {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement = connection.prepareStatement("SELECT id FROM CURRENCY WHERE id = ?")
                statement.setLong(1, id)

                val resultSet = statement.executeQuery()
                resultSet.next()
                val result = resultSet.fetchSize

                statement.close()

                return result == 1
            }
        } catch (e: Throwable) {
            logger.warn("Error checking if currency_id=$id exists in database", e)
        }
        return null
    }

    fun trySaveCurrencyAndReturnId(currencyName: String): Result<Long> {
        try {
            databaseAccessRepository.getDataSource().connection.use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO CURRENCY (currency_name) VALUES (?) ON CONFLICT (currency_name) DO UPDATE SET currency_name = EXCLUDED.currency_name RETURNING id")
                statement.setString(1, currencyName)

                val resultSet = statement.executeQuery()
                resultSet.next()

                val unitId = resultSet.getLong(1)
                statement.close()

                logger.info("Currency id for name $currencyName is saved to table CURRENCY in database")
                return Result.success(unitId)
            }
        } catch (e: Throwable) {
            logger.warn("Error saving new currency id for name $currencyName\n", e)

            return Result.failure(Throwable(e))
        }
    }
}
