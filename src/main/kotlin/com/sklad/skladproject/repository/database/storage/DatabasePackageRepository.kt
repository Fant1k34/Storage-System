package com.sklad.skladproject.repository.database.storage

import com.sklad.skladproject.domain.Package
import com.sklad.skladproject.dto.PackageDto
import com.sklad.skladproject.repository.database.storage.tables.MeasureUnitTable
import com.sklad.skladproject.repository.database.storage.tables.PackageTable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DatabasePackageRepository(
    val packageTable: PackageTable,
    val measureUnitTable: MeasureUnitTable,
) {
    val logger = LoggerFactory.getLogger("DatabasePackageRepository")

    fun trySavePackage(pack: Package): Result<Long> {
        val measureIdExists = measureUnitTable.checkIfMeasureUnitId(pack.weight.unitId)
        if (measureIdExists == null) {
            logger.error("Measure unit with id ${pack.weight.unitId} does not exist in database")
            return Result.failure(Throwable("Measure unit does not exist in database"))
        }

        return packageTable.trySavePackageAndReturnId(
            name = pack.name,
            weight = pack.weight.number,
            unitId = pack.weight.unitId
        )
    }

    fun getAllPackages(): List<PackageDto> = packageTable.tryGetAllPackages()

    fun getPackagesByName(name: String): List<PackageDto> = packageTable.tryGetPackagesByName(name)
}
