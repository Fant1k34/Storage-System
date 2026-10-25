package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.PurchaseRequestDto
import com.sklad.skladproject.services.PurchaseRequestService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/")
class PurchaseOperationController(
    val purchaseRequestService: PurchaseRequestService
) {
    private val logger = LoggerFactory.getLogger("PurchaseOperationController")

    @ResponseBody
    @GetMapping("/health-check")
    fun health_check(): String {
        return "OK"
    }

    @ResponseBody
    @PostMapping("/v1/add-purchase-operation")
    fun addItemToStorage(@RequestBody purchaseRequest: PurchaseRequestDto): Boolean {
        logger.info("Handling purchase request $purchaseRequest")

        val purchaseRequestDomain = purchaseRequestService.convertToPurchaseRequest(purchaseRequest)
        val result = purchaseRequestService.savePurchase(purchaseRequestDomain)

        val error = result.exceptionOrNull()
        if (error is IllegalArgumentException) {
            logger.warn("Purchase request rejected: ${error.message}")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, error.message)
        }

        return result.isSuccess
    }
}
