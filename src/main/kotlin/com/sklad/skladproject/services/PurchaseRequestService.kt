package com.sklad.skladproject.services

import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatter
import com.sklad.skladproject.dto.PurchaseOperationDto
import com.sklad.skladproject.domain.PurchaseRequest
import com.sklad.skladproject.dto.PurchaseRequestDto
import com.sklad.skladproject.repository.database.storage.DatabasePurchaseOperationHistoryRepository
import org.springframework.stereotype.Service

@Service
class PurchaseRequestService(
    val databasePurchaseOperationHistoryRepository: DatabasePurchaseOperationHistoryRepository
) {
    fun convertToPurchaseRequest(purchaseRequestDto: PurchaseRequestDto): PurchaseRequest {
        return PurchaseRequest(
            date = purchaseRequestDto.date,
            itemId = purchaseRequestDto.itemId,
            quantity = purchaseRequestDto.quantity,
            quantityUnitId = purchaseRequestDto.quantityUnitId,
            packageId = purchaseRequestDto.packageId,
            boughtPrice = purchaseRequestDto.boughtPrice,
            currency = purchaseRequestDto.currency,
        )
    }

    // All problems are collected into one message, so the client can fix them in a single round trip
    fun validatePurchaseRequest(purchaseRequest: PurchaseRequest): Result<Unit> {
        val errors = mutableListOf<String>()

        if (!Iso8601DateValidator.isValid(purchaseRequest.date)) {
            errors.add("Date '${purchaseRequest.date}' is not valid. Expected format: ${Iso8601DateValidator.EXPECTED_FORMAT_DESCRIPTION}")
        }
        if (!(purchaseRequest.quantity > 0)) {
            errors.add("Quantity must be positive, got ${purchaseRequest.quantity}")
        }
        if (!(purchaseRequest.boughtPrice > 0)) {
            errors.add("Bought price must be positive, got ${purchaseRequest.boughtPrice}")
        }

        if (errors.isNotEmpty()) {
            return Result.failure(IllegalArgumentException(errors.joinToString("; ")))
        }

        return Result.success(Unit)
    }

    fun savePurchase(purchaseRequest: PurchaseRequest): Result<Long> {
        validatePurchaseRequest(purchaseRequest).onFailure { return Result.failure(it) }

        return databasePurchaseOperationHistoryRepository.trySavePurchase(purchaseRequest)
    }


    // Query parameters are validated here, so the controller only maps the failure to HTTP 400
    fun validatePurchaseOperationsQuery(date: String, page: Int, pageSize: Int): Result<Unit> {
        val errors = mutableListOf<String>()

        if (!isIsoLocalDate(date)) {
            errors.add("Date '$date' is not valid. Expected a calendar date in ISO 8601 format, for example 2026-10-25")
        }
        if (page < FIRST_PAGE) {
            errors.add("Page must be $FIRST_PAGE or greater, got $page")
        }
        if (pageSize <= 0) {
            errors.add("Page size must be positive, got $pageSize")
        }
        if (pageSize > MAX_PAGE_SIZE) {
            errors.add("Page size must not exceed $MAX_PAGE_SIZE, got $pageSize")
        }

        if (errors.isNotEmpty()) {
            return Result.failure(IllegalArgumentException(errors.joinToString("; ")))
        }

        return Result.success(Unit)
    }

    // Pages are numbered from FIRST_PAGE; the page is turned into LIMIT/OFFSET for the database
    fun getPurchaseOperationsByDate(date: String, page: Int, pageSize: Int): Result<List<PurchaseOperationDto>> {
        validatePurchaseOperationsQuery(date, page, pageSize).onFailure { return Result.failure(it) }

        val offset = (page - FIRST_PAGE) * pageSize
        return Result.success(databasePurchaseOperationHistoryRepository.getPurchaseOperationsByDate(date, pageSize, offset))
    }

    private fun isIsoLocalDate(date: String): Boolean {
        return try {
            DateTimeFormatter.ISO_LOCAL_DATE.parse(date)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    companion object {
        const val FIRST_PAGE = 0
        const val DEFAULT_PAGE_SIZE = 50
        const val MAX_PAGE_SIZE = 500
    }
}
