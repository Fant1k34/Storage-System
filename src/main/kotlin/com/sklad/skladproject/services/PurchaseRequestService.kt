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

    fun savePurchase(purchaseRequest: PurchaseRequest): Result<Long> {
        val result = databasePurchaseOperationHistoryRepository.trySavePurchase(purchaseRequest)

        return result
    }
}