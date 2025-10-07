package com.sklad.skladproject.controllers

import com.sklad.skladproject.domain.PurchaseItem
import com.sklad.skladproject.dto.AddingPurchaseResultDto
import com.sklad.skladproject.dto.PurchaseDto
import com.sklad.skladproject.repository.PurchaseBuilder
import com.sklad.skladproject.services.SavePurchaseService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@RestController
@RequestMapping("/")
class PurchaseOperationController(
    val purchaseBuilder: PurchaseBuilder,
    val savePurchaseService: SavePurchaseService
) {
    private val logger = LoggerFactory.getLogger("PurchaseOperationController")

    @ResponseBody
    @GetMapping("/v1/purchase-operations")
    fun page(@RequestParam("page") page: Int): List<PurchaseItem> {
        val limit = 10
        val offset = page * limit

        return savePurchaseService.tryGetPurchaseOperations(limit, offset)
    }

    @ResponseBody
    @PostMapping("/v1/add-purchase-operation")
    fun addItemToStorage(@RequestBody purchaseOperation: PurchaseDto): AddingPurchaseResultDto {
        val purchase = purchaseBuilder.associatePurchaseDtoWithPurchaseItem(purchaseOperation)

        return purchase.fold(
            AddingPurchaseResultDto(
                emptyList(),
                emptyList()
            )
        ) { acc, (purchaseItemDto, purchaseItem) ->
            if (purchaseItem == null) {
                logger.warn("Error occurred while creating purchase item for purchase item dto: $purchaseItemDto. Skipping this purchase item.")
                return@fold AddingPurchaseResultDto(acc.added, acc.failed + listOf(purchaseItemDto.describingString))
            }

            val result = savePurchaseService.savePurchase(purchaseItem)
            if (result.isFailure) {
                logger.warn("Error occurred while saving purchase item for purchase item dto: $purchaseItemDto. Skipping this purchase item.")
                return@fold AddingPurchaseResultDto(acc.added, acc.failed + listOf(purchaseItemDto.describingString))
            }

            return@fold AddingPurchaseResultDto(acc.added + listOf(purchaseItemDto.describingString), acc.failed)
        }
    }
}
