package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.CurrencyDto
import com.sklad.skladproject.dto.CurrencyRequestDto
import com.sklad.skladproject.services.CurrencyService
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
class CurrencyController(
    val currencyService: CurrencyService
) {
    private val logger = LoggerFactory.getLogger("CurrencyController")

    @ResponseBody
    @PostMapping("/v1/add-currency")
    fun addCurrency(@RequestBody currencyRequest: CurrencyRequestDto): Boolean {
        logger.info("Handling add currency request $currencyRequest")

        val currencyDomain = currencyService.convertToCurrency(currencyRequest)
        val result = currencyService.saveCurrency(currencyDomain)

        return result.isSuccess
    }

    @ResponseBody
    @GetMapping("/v1/get-all-currencies")
    fun getAllCurrencies(): List<CurrencyDto> {
        logger.info("Handling get all currencies request")

        return currencyService.getAllCurrencies()
    }

    @ResponseBody
    @GetMapping("/v1/get-currency")
    fun getCurrency(@RequestParam("name") name: String): ResponseEntity<CurrencyDto> {
        logger.info("Handling get currency request for name $name")

        val currency = currencyService.getCurrencyByName(name) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(currency)
    }
}
