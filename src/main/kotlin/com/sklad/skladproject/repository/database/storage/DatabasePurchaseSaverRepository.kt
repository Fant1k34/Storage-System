package com.sklad.skladproject.repository.database.storage

import com.sklad.skladproject.domain.PurchaseItem
import com.sklad.skladproject.repository.PurchaseSaverRepository
import com.sklad.skladproject.repository.database.storage.tables.ListingItemStorage
import com.sklad.skladproject.repository.database.storage.tables.MeasureUnitStorage
import com.sklad.skladproject.repository.database.storage.tables.PackageStorage
import com.sklad.skladproject.repository.database.storage.tables.PurchaseItemStorage
import com.sklad.skladproject.repository.database.storage.tables.StorageStorage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DatabasePurchaseSaverRepository(
    val listingItemStorage: ListingItemStorage,
    val measureUnitStorage: MeasureUnitStorage,
    val packageStorage: PackageStorage,
    val storageStorage: StorageStorage,
    val purchaseItemStorage: PurchaseItemStorage
) : PurchaseSaverRepository {
    private val logger = LoggerFactory.getLogger("DatabasePurchaseSaverRepository")

    override fun trySavePurchaseItem(purchaseItem: PurchaseItem): Boolean {
        listingItemStorage.trySaveListingItem(purchaseItem.listingItem)
        val listingItemId = listingItemStorage.tryGetListingItemId(purchaseItem.listingItem)
        if (listingItemId == null) {
            logger.error("Listing item ${purchaseItem.listingItem} does not exist in database, despite it was recently added")
            return false
        }

        // trySaveMeasureUnit(purchaseItem.pack.weight.unit.unitName) -- should it be saved?
        val packMeasureUnitId = measureUnitStorage.tryGetMeasureUnitId(purchaseItem.pack.weight.unit.unitName)
        if (packMeasureUnitId == null) {
            logger.error("Measure unit for package ${purchaseItem.pack.weight.unit.unitName} does not exist in database")
            return false
        }
        packageStorage.trySavePackage(purchaseItem.pack.name, purchaseItem.pack.weight.number, packMeasureUnitId)
        val packageId =
            packageStorage.tryGetPackageId(purchaseItem.pack.name, purchaseItem.pack.weight.number, packMeasureUnitId)
        if (packageId == null) {
            logger.error("Package ${purchaseItem.pack.name} with weight ${purchaseItem.pack.weight.number} (measure unit id: $packMeasureUnitId) does not exist in database, despite it was recently added")
            return false
        }

        val storageId = storageStorage.tryGetStorageId(purchaseItem.storageName)
        if (storageId == null) {
            logger.error("Storage with name ${purchaseItem.storageName} does not exist in database")
            return false
        }

        // trySaveMeasureUnit(purchaseItem.quantity.unit.unitName) -- should it be saved?
        val purchaseItemMeasureId = measureUnitStorage.tryGetMeasureUnitId(purchaseItem.quantity.unit.unitName)
        if (purchaseItemMeasureId == null) {
            logger.error("Measure unit for purchase item ${purchaseItem.quantity.unit.unitName} does not exist in database")
            return false
        }

        val nettItemQuantity = purchaseItem.quantity.number
        val nettItemQuantityUnitId = measureUnitStorage.tryGetMeasureUnitId(purchaseItem.quantity.unit.unitName)
        if (nettItemQuantityUnitId == null) {
            logger.error("Measure unit for item quantity ${purchaseItem.quantity.unit.unitName} does not exist in database")
            return false
        }

        val boughtPrice = purchaseItem.boughtPrice.number
        val boughtPriceUnitId = measureUnitStorage.tryGetMeasureUnitId(purchaseItem.boughtPrice.unit.unitName)
        if (boughtPriceUnitId == null) {
            logger.error("Measure unit for item quantity ${purchaseItem.boughtPrice.unit.unitName} does not exist in database")
            return false
        }

        val soldPrice = purchaseItem.soldPrice.number
        val soldPriceUnitId = measureUnitStorage.tryGetMeasureUnitId(purchaseItem.soldPrice.unit.unitName)
        if (soldPriceUnitId == null) {
            logger.error("Measure unit for item quantity ${purchaseItem.soldPrice.unit.unitName} does not exist in database")
            return false
        }

        purchaseItemStorage.trySavePurchase(
            listingItemId,
            storageId,
            nettItemQuantity,
            nettItemQuantityUnitId,
            boughtPrice,
            boughtPriceUnitId,
            soldPrice,
            soldPriceUnitId
        )
        return true
    }
}