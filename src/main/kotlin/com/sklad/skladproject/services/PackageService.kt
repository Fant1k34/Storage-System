package com.sklad.skladproject.services

import com.sklad.skladproject.domain.Package
import com.sklad.skladproject.domain.Quantity
import com.sklad.skladproject.dto.PackageDto
import com.sklad.skladproject.dto.PackageRequestDto
import com.sklad.skladproject.repository.database.storage.DatabasePackageRepository
import org.springframework.stereotype.Service

@Service
class PackageService(
    val databasePackageRepository: DatabasePackageRepository
) {
    fun convertToPackage(packageRequestDto: PackageRequestDto): Package {
        return Package(
            name = packageRequestDto.name,
            weight = Quantity(
                number = packageRequestDto.weight,
                unitId = packageRequestDto.weightUnitId
            )
        )
    }

    fun savePackage(pack: Package): Result<PackageDto> {
        return databasePackageRepository.trySavePackage(pack).map { id ->
            val unitName = databasePackageRepository.getMeasureUnitById(pack.weight.unitId)?.name ?: ""
            PackageDto(
                id = id,
                name = pack.name,
                weight = pack.weight.number,
                weightUnitId = pack.weight.unitId,
                weightUnitName = unitName,
            )
        }
    }

    fun getAllPackages(): List<PackageDto> = databasePackageRepository.getAllPackages()

    fun getPackagesByName(name: String): List<PackageDto> = databasePackageRepository.getPackagesByName(name)
}
