package com.sklad.skladproject.services

import com.sklad.skladproject.dto.CurrencyRequestDto
import com.sklad.skladproject.repository.database.storage.DatabaseCurrencyRepository
import org.springframework.stereotype.Service

@Service
class CurrencyService(
    val databaseCurrencyRepository: DatabaseCurrencyRepository
) {
    fun convertToCurrency(currencyRequestDto: CurrencyRequestDto): String {
        return currencyRequestDto.name
    }

    fun saveCurrency(currency: String): Result<Long> {
        return databaseCurrencyRepository.trySaveCurrency(currency)
    }
}
