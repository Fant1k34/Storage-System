package com.sklad.skladproject.repository

import com.sklad.skladproject.domain.Package
import com.sklad.skladproject.domain.PurchaseItem
import com.sklad.skladproject.domain.Quantity
import com.sklad.skladproject.domain.Unit
import com.sklad.skladproject.dto.PurchaseDto
import com.sklad.skladproject.dto.PurchaseItemDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PurchaseBuilder {
    val logger = LoggerFactory.getLogger("PurchaseBuilder")

    private fun tryCreatePackage(purchaseItemDto: PurchaseItemDto): Package? {
        val packName = purchaseItemDto.packName
        if (packName == null || purchaseItemDto.packWeight == null) return null

        val quantity = tryCreateQuantity(purchaseItemDto.packWeight, purchaseItemDto.packUnit) ?: return null

        return Package(packName, quantity)
    }

    fun tryCreatePackage(packageName: String, packageWeight: Double, packageUnit: String): Package? {
        val quantity = tryCreateQuantity(packageWeight, packageUnit) ?: return null
        return Package(packageName, quantity)
    }

    fun tryCreateQuantity(amount: Double, unitName: String): Quantity? {
        val measureUnit = tryCreateUnit(unitName)

        if (measureUnit == null) {
            return null
        }

        return Quantity(amount, measureUnit)
    }

    private fun tryCreateUnit(unitName: String): Unit? {
        return when (unitName) {
            Unit.AMOUNT.unitName -> Unit.AMOUNT
            Unit.KG.unitName -> Unit.KG
            Unit.G.unitName -> Unit.G
            Unit.L.unitName -> Unit.L
            Unit.RUB.unitName -> Unit.RUB
            else -> null
        }
    }

    private fun tryCreatePurchaseItem(purchaseItemDto: PurchaseItemDto): PurchaseItem? {
        val date = purchaseItemDto.date
        val listingItem = purchaseItemDto.name
        val quantity = tryCreateQuantity(purchaseItemDto.quantity, purchaseItemDto.quantityUnit)
            ?: return null.also { logger.error("Cannot create quantity for purchase item") }
        val packAmount = purchaseItemDto.packageAmount
        val pack = tryCreatePackage(purchaseItemDto)
            ?: return null.also { logger.error("Cannot create package for purchase item") }
        val boughtPrice = tryCreateQuantity(purchaseItemDto.boughtPrice, purchaseItemDto.boughtPriceUnit)
            ?: return null.also { logger.error("Cannot create bought price for purchase item") }

//        val status = PurchaseItemState.SAVED_TO_HISTORY
        val timestamp = System.currentTimeMillis()

        return PurchaseItem(
            date,
            listingItem,
            quantity,
            packAmount,
            pack,
            boughtPrice,
            timestamp
        )
    }

    fun associatePurchaseDtoWithPurchaseItem(purchaseDto: PurchaseDto): List<Pair<PurchaseItemDto, PurchaseItem?>> {
        logger.info("Trying to create purchase from dto: $purchaseDto")

        return purchaseDto.items.map { item ->
            logger.info("Trying to create purchase item from dto: $item")
            item to tryCreatePurchaseItem(item)
        }
    }
}