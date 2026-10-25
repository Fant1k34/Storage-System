package com.sklad.skladproject.repository.database.storage

import com.sklad.skladproject.domain.Package
import com.sklad.skladproject.dto.MeasureUnitDto
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
        if (pack.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Package name must not be blank"))
        }
        if (!(pack.weight.number > 0)) {
            return Result.failure(IllegalArgumentException("Package weight must be positive, got ${pack.weight.number}"))
        }

        val measureIdExists = measureUnitTable.checkIfMeasureUnitId(pack.weight.unitId)
        if (measureIdExists == null) {
            return Result.failure(Throwable("Error checking measure unit in database"))
        }
        if (!measureIdExists) {
            logger.error("Measure unit with id ${pack.weight.unitId} does not exist in database")
            return Result.failure(IllegalArgumentException("Measure unit with id ${pack.weight.unitId} does not exist"))
        }

        return packageTable.trySavePackageAndReturnId(
            name = pack.name,
            weight = pack.weight.number,
            unitId = pack.weight.unitId
        )
    }

    fun getAllPackages(): List<PackageDto> = packageTable.tryGetAllPackages()

    fun getPackagesByName(name: String): List<PackageDto> = packageTable.tryGetPackagesByName(name)

    fun getMeasureUnitById(id: Long): MeasureUnitDto? = measureUnitTable.tryGetMeasureUnitById(id)
}
