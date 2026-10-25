package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.MeasureUnitDto
import com.sklad.skladproject.dto.MeasureUnitRequestDto
import com.sklad.skladproject.services.MeasureUnitService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Measure units", description = "Units for quantities and package weights")
@RestController
@RequestMapping("/")
class MeasureUnitController(
    val measureUnitService: MeasureUnitService
) {
    private val logger = LoggerFactory.getLogger("MeasureUnitController")

    @Operation(summary = "Add a measure unit", description = "Idempotent: an existing a measure unit is not duplicated.")
    @ApiResponse(responseCode = "200", description = "The record as stored (created or already present), with its id")
    @ApiResponse(responseCode = "400", description = "Validation failed; `message` explains why", content = [Content()])
    @ApiResponse(responseCode = "500", description = "Database failure", content = [Content()])
    @ResponseBody
    @PostMapping("/v1/add-measure")
    fun addMeasureUnit(@RequestBody measureUnitRequest: MeasureUnitRequestDto): MeasureUnitDto {
        logger.info("Handling add measure unit request $measureUnitRequest")

        val measureUnitDomain = measureUnitService.convertToMeasureUnit(measureUnitRequest)
        val result = measureUnitService.saveMeasureUnit(measureUnitDomain)

        return result.getOrElse { throw it.toResponseStatusException() }
    }

    @Operation(summary = "List all measure units", description = "Ordered by id.")
    @ResponseBody
    @GetMapping("/v1/get-all-measures")
    fun getAllMeasureUnits(): List<MeasureUnitDto> {
        logger.info("Handling get all measure units request")

        return measureUnitService.getAllMeasureUnits()
    }

    @Operation(summary = "Get a measure unit by name")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "404", description = "No record with this name", content = [Content()])
    @ResponseBody
    @GetMapping("/v1/get-measure")
    fun getMeasureUnit(@Parameter(description = "Exact name to look up", example = "kg") @RequestParam("name") name: String): ResponseEntity<MeasureUnitDto> {
        logger.info("Handling get measure unit request for name $name")

        val measureUnit = measureUnitService.getMeasureUnitByName(name) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(measureUnit)
    }
}
