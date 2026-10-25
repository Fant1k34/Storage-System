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

    fun savePackage(pack: Package): Result<Long> {
        return databasePackageRepository.trySavePackage(pack)
    }

    fun getAllPackages(): List<PackageDto> = databasePackageRepository.getAllPackages()

    fun getPackagesByName(name: String): List<PackageDto> = databasePackageRepository.getPackagesByName(name)
}
