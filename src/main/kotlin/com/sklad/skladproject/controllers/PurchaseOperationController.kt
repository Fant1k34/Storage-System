package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.PurchaseItemDto
import com.sklad.skladproject.dto.PurchaseRequestDto
import com.sklad.skladproject.services.PurchaseRequestService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

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
    @GetMapping("/v1/purchase-operations")
    fun page(@RequestParam("page") page: Int): List<PurchaseItemDto> {
        TODO()
    }

    @ResponseBody
    @PostMapping("/v1/add-purchase-operation")
    fun addItemToStorage(@RequestBody purchaseRequest: PurchaseRequestDto): Boolean {
        logger.info("Handling purchase request $purchaseRequest")

        val purchaseRequestDomain = purchaseRequestService.convertToPurchaseRequest(purchaseRequest)
        val result = purchaseRequestService.savePurchase(purchaseRequestDomain)

        return result.isSuccess
    }
}
