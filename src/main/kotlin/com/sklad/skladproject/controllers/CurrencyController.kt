package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.CurrencyDto
import com.sklad.skladproject.dto.CurrencyRequestDto
import com.sklad.skladproject.services.CurrencyService
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

@Tag(name = "Currencies", description = "Currencies used for purchase prices")
@RestController
@RequestMapping("/")
class CurrencyController(
    val currencyService: CurrencyService
) {
    private val logger = LoggerFactory.getLogger("CurrencyController")

    @Operation(summary = "Add a currency", description = "Idempotent: an existing a currency is not duplicated.")
    @ApiResponse(responseCode = "200", description = "The record as stored (created or already present), with its id")
    @ApiResponse(responseCode = "400", description = "Validation failed; `message` explains why", content = [Content()])
    @ApiResponse(responseCode = "500", description = "Database failure", content = [Content()])
    @ResponseBody
    @PostMapping("/v1/add-currency")
    fun addCurrency(@RequestBody currencyRequest: CurrencyRequestDto): CurrencyDto {
        logger.info("Handling add currency request $currencyRequest")

        val currencyDomain = currencyService.convertToCurrency(currencyRequest)
        val result = currencyService.saveCurrency(currencyDomain)

        return result.getOrElse { throw it.toResponseStatusException() }
    }

    @Operation(summary = "List all currencies", description = "Ordered by id.")
    @ResponseBody
    @GetMapping("/v1/get-all-currencies")
    fun getAllCurrencies(): List<CurrencyDto> {
        logger.info("Handling get all currencies request")

        return currencyService.getAllCurrencies()
    }

    @Operation(summary = "Get a currency by name")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "404", description = "No record with this name", content = [Content()])
    @ResponseBody
    @GetMapping("/v1/get-currency")
    fun getCurrency(@Parameter(description = "Exact name to look up", example = "rub") @RequestParam("name") name: String): ResponseEntity<CurrencyDto> {
        logger.info("Handling get currency request for name $name")

        val currency = currencyService.getCurrencyByName(name) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(currency)
    }
}
