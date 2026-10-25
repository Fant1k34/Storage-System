package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.MeasureUnitDto
import com.sklad.skladproject.dto.MeasureUnitRequestDto
import com.sklad.skladproject.services.MeasureUnitService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class MeasureUnitController(
    val measureUnitService: MeasureUnitService
) {
    private val logger = LoggerFactory.getLogger("MeasureUnitController")

    @ResponseBody
    @PostMapping("/v1/add-measure")
    fun addMeasureUnit(@RequestBody measureUnitRequest: MeasureUnitRequestDto): Boolean {
        logger.info("Handling add measure unit request $measureUnitRequest")

        val measureUnitDomain = measureUnitService.convertToMeasureUnit(measureUnitRequest)
        val result = measureUnitService.saveMeasureUnit(measureUnitDomain)

        return result.isSuccess
    }

    @ResponseBody
    @GetMapping("/v1/get-all-measures")
    fun getAllMeasureUnits(): List<MeasureUnitDto> {
        logger.info("Handling get all measure units request")

        return measureUnitService.getAllMeasureUnits()
    }

    @ResponseBody
    @GetMapping("/v1/get-measure")
    fun getMeasureUnit(@RequestParam("name") name: String): ResponseEntity<MeasureUnitDto> {
        logger.info("Handling get measure unit request for name $name")

        val measureUnit = measureUnitService.getMeasureUnitByName(name) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(measureUnit)
    }
}
