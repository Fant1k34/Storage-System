package com.sklad.skladproject.repository

import com.sklad.skladproject.domain.Package
import com.sklad.skladproject.domain.Purchase
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

    fun tryCreatePackage(purchaseItemDto: PurchaseItemDto): Package? {
        val packName = purchaseItemDto.packName
        if (packName == null || purchaseItemDto.packWeight == null) return null

        val quantity = tryCreateQuantity(purchaseItemDto.packWeight, purchaseItemDto.packMeasureUnit)
        return if (quantity != null) Package(packName, quantity) else null
    }

    private fun tryCreateQuantity(amount: Double, unitName: String): Quantity? {
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

    fun tryCreatePurchaseItem(purchaseItemDto: PurchaseItemDto): PurchaseItem? {
        val listingItem = purchaseItemDto.name
        val quantity =
            tryCreateQuantity(purchaseItemDto.amount, purchaseItemDto.amountMeasureUnit)
                ?: return null.also { logger.error("Cannot create quantity for purchase item") }
        val pack = tryCreatePackage(purchaseItemDto)
            ?: return null.also { logger.error("Cannot create package for purchase item") }
        val storageName = purchaseItemDto.storage
        val boughtPrice =
            tryCreateQuantity(purchaseItemDto.boughtPrice, purchaseItemDto.boughtPriceMeasureUnit)
                ?: return null.also { logger.error("Cannot create bought price for purchase item") }
        val soldPrice = purchaseItemDto.soldPrice?.let {
            purchaseItemDto.soldPriceMeasureUnit?.let {
                tryCreateQuantity(
                    purchaseItemDto.soldPrice,
                    purchaseItemDto.soldPriceMeasureUnit
                )
            }
        } ?: return null.also { logger.error("Cannot create sold price for purchase item") }

        return PurchaseItem(listingItem, quantity, pack, storageName, boughtPrice, soldPrice)
    }

    fun tryCreatePurchase(purchaseDto: PurchaseDto): Purchase? {
        logger.info("Trying to create purchase from dto: $purchaseDto")
        val itemsList = purchaseDto.items.map { item ->
            logger.info("Trying to create purchase item from dto: $item")
            tryCreatePurchaseItem(item) ?: return null
        }

        return Purchase(itemsList, purchaseDto.date)
    }
}