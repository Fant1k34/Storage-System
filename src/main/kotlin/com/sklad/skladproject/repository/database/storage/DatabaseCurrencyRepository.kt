package com.sklad.skladproject.repository.database.storage

import com.sklad.skladproject.repository.database.storage.tables.CurrencyTable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DatabaseCurrencyRepository(
    val currencyTable: CurrencyTable,
) {
    val logger = LoggerFactory.getLogger("DatabaseCurrencyRepository")

    fun trySaveCurrency(currency: String): Result<Long> {
        if (currency.isBlank()) {
            logger.error("Currency name must not be blank")
            return Result.failure(Throwable("Currency name must not be blank"))
        }

        return currencyTable.trySaveCurrencyAndReturnId(currency)
    }
}
