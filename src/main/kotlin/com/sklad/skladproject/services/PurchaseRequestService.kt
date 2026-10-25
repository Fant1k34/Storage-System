package com.sklad.skladproject.services

import com.sklad.skladproject.domain.PurchaseItem
import com.sklad.skladproject.domain.PurchaseRequest
import com.sklad.skladproject.dto.PurchaseRequestDto
import com.sklad.skladproject.repository.database.storage.DatabasePurchaseOperationHistoryRepository
//import com.sklad.skladproject.repository.database.storage.DatabasePurchaseSaverRepository
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
            packageAmount = purchaseRequestDto.packageAmount,
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
        if (purchaseRequest.packageAmount <= 0) {
            errors.add("Package amount must be positive, got ${purchaseRequest.packageAmount}")
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
}