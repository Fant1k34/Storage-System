package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.CurrencyRequestDto
import com.sklad.skladproject.services.CurrencyService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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
}
