package com.sklad.skladproject.services

import com.sklad.skladproject.dto.MeasureUnitDto
import com.sklad.skladproject.dto.MeasureUnitRequestDto
import com.sklad.skladproject.repository.database.storage.DatabaseMeasureUnitRepository
import org.springframework.stereotype.Service

@Service
class MeasureUnitService(
    val databaseMeasureUnitRepository: DatabaseMeasureUnitRepository
) {
    fun convertToMeasureUnit(measureUnitRequestDto: MeasureUnitRequestDto): String {
        return measureUnitRequestDto.name
    }

    fun saveMeasureUnit(measureUnit: String): Result<MeasureUnitDto> {
        return databaseMeasureUnitRepository.trySaveMeasureUnit(measureUnit).map { id -> MeasureUnitDto(id = id, name = measureUnit) }
    }

    fun getAllMeasureUnits(): List<MeasureUnitDto> = databaseMeasureUnitRepository.getAllMeasureUnits()

    fun getMeasureUnitByName(name: String): MeasureUnitDto? = databaseMeasureUnitRepository.getMeasureUnitByName(name)
}
