//package com.sklad.skladproject.repository.database.storage
//
//import com.sklad.skladproject.domain.PurchaseItem
//import com.sklad.skladproject.repository.PurchaseSaverRepository
//import com.sklad.skladproject.repository.database.storage.tables.ListingItemTable
//import com.sklad.skladproject.repository.database.storage.tables.MeasureUnitTable
//import com.sklad.skladproject.repository.database.storage.tables.PackageTable
//import com.sklad.skladproject.repository.database.storage.tables.PurchaseItemInStoreTable
//import com.sklad.skladproject.repository.database.storage.tables.PurchaseItemTable
//import com.sklad.skladproject.repository.database.storage.tables.StorageTable
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Repository
//
//@Repository
//class DatabasePurchaseSaverRepository(
//    val listingItemTable: ListingItemTable,
//    val measureUnitTable: MeasureUnitTable,
//    val packageTable: PackageTable,
//    val storageTable: StorageTable,
//    val purchaseItemTable: PurchaseItemTable,
//    val purchaseItemInStoreTable: PurchaseItemInStoreTable,
//) : PurchaseSaverRepository {
//    private val logger = LoggerFactory.getLogger("DatabasePurchaseSaverRepository")
//
//    override fun trySavePurchaseItem(purchaseItem: PurchaseItem): Boolean {
//        listingItemTable.trySaveListingItemAndReturnId(purchaseItem.listingItem)
//        val listingItemId = listingItemTable.tryGetListingItemId(purchaseItem.listingItem)
//        if (listingItemId == null) {
//            logger.error("Listing item ${purchaseItem.listingItem} does not exist in database, despite it was recently added")
//            return false
//        }
//
//        // trySaveMeasureUnit(purchaseItem.pack.weight.unit.unitName) -- should it be saved?
//        val packMeasureUnitId = measureUnitTable.tryGetMeasureUnitId(purchaseItem.pack.weight.unit.unitName)
//        if (packMeasureUnitId == null) {
//            logger.error("Measure unit for package ${purchaseItem.pack.weight.unit.unitName} does not exist in database")
//            return false
//        }
//        packageTable.trySavePackage(purchaseItem.pack.name, purchaseItem.pack.weight.number, packMeasureUnitId)
//        val packageId =
//            packageTable.tryGetPackageId(purchaseItem.pack.name, purchaseItem.pack.weight.number, packMeasureUnitId)
//        if (packageId == null) {
//            logger.error("Package ${purchaseItem.pack.name} with weight ${purchaseItem.pack.weight.number} (measure unit id: $packMeasureUnitId) does not exist in database, despite it was recently added")
//            return false
//        }
//
//        val storageId = storageTable.tryGetStorageId(purchaseItem.storageName)
//        if (storageId == null) {
//            logger.error("Storage with name ${purchaseItem.storageName} does not exist in database")
//            return false
//        }
//
//        // trySaveMeasureUnit(purchaseItem.quantity.unit.unitName) -- should it be saved?
//        val purchaseItemMeasureId = measureUnitTable.tryGetMeasureUnitId(purchaseItem.quantity.unit.unitName)
//        if (purchaseItemMeasureId == null) {
//            logger.error("Measure unit for purchase item ${purchaseItem.quantity.unit.unitName} does not exist in database")
//            return false
//        }
//
//        val nettItemQuantity = purchaseItem.quantity.number
//        val nettItemQuantityUnitId = measureUnitTable.tryGetMeasureUnitId(purchaseItem.quantity.unit.unitName)
//        if (nettItemQuantityUnitId == null) {
//            logger.error("Measure unit for item quantity ${purchaseItem.quantity.unit.unitName} does not exist in database")
//            return false
//        }
//
//        val soldPrice = purchaseItem.soldPrice.number
//        val soldPriceUnitId = measureUnitTable.tryGetMeasureUnitId(purchaseItem.soldPrice.unit.unitName)
//        if (soldPriceUnitId == null) {
//            logger.error("Measure unit for item quantity ${purchaseItem.soldPrice.unit.unitName} does not exist in database")
//            return false
//        }
//
//        val operationId = purchaseItemTable.trySavePurchaseAndReturnId(
//            listingItemId,
//            storageId,
//            nettItemQuantity,
//            nettItemQuantityUnitId,
//            soldPrice,
//            soldPriceUnitId
//        )
//
//        if (operationId == null) {
//            return false
//        }
//
//        purchaseItemInStoreTable.trySavePurchaseItemWithPackage(operationId, packageId)
//
//        return true
//    }
//}