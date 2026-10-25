package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.MeasureUnitRequestDto
import com.sklad.skladproject.services.MeasureUnitService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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
}
