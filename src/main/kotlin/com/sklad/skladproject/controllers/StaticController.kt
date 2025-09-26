package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.PurchaseDto
import com.sklad.skladproject.repository.PurchaseBuilder
import com.sklad.skladproject.services.SavePurchaseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@RestController
@RequestMapping("/")
class StaticController(
    val purchaseBuilder: PurchaseBuilder,
    val savePurchaseService: SavePurchaseService
) {
    @ResponseBody
    @GetMapping("/")
    fun page(): String {
        return "index"
    }

    @ResponseBody
    @PostMapping("/v1/add-item-to-storage")
    fun addItemToStorage(@RequestBody purchase: PurchaseDto): String {
        val purchase = purchaseBuilder.tryCreatePurchase(purchase)

        if (purchase == null) {
            return "Invalid purchase"
        }

        savePurchaseService.savePurchase(purchase)
        return "OK"
    }
}
