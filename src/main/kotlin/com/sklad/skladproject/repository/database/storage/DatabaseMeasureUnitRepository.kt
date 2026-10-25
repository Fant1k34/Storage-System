package com.sklad.skladproject.repository.database.storage

import com.sklad.skladproject.dto.MeasureUnitDto
import com.sklad.skladproject.repository.database.storage.tables.MeasureUnitTable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DatabaseMeasureUnitRepository(
    val measureUnitTable: MeasureUnitTable,
) {
    val logger = LoggerFactory.getLogger("DatabaseMeasureUnitRepository")

    fun trySaveMeasureUnit(measureUnit: String): Result<Long> {
        if (measureUnit.isBlank()) {
            logger.error("Measure unit name must not be blank")
            return Result.failure(Throwable("Measure unit name must not be blank"))
        }

        return measureUnitTable.trySaveMeasureUnitAndReturnId(measureUnit)
    }

    fun getAllMeasureUnits(): List<MeasureUnitDto> = measureUnitTable.tryGetAllMeasureUnits()

    fun getMeasureUnitByName(name: String): MeasureUnitDto? = measureUnitTable.tryGetMeasureUnitByName(name)
}
