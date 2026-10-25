package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.PurchaseOperationDto
import com.sklad.skladproject.dto.PurchaseRequestDto
import com.sklad.skladproject.services.PurchaseRequestService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Purchases", description = "Purchase operation history")
@RestController
@RequestMapping("/")
class PurchaseOperationController(
    val purchaseRequestService: PurchaseRequestService
) {
    private val logger = LoggerFactory.getLogger("PurchaseOperationController")

    @Operation(summary = "Liveness check", tags = ["Service"])
    @ResponseBody
    @GetMapping("/health-check")
    fun health_check(): String {
        return "OK"
    }

    @Operation(
        summary = "List purchase operations for a date, paginated",
        description = "Returns purchases whose stored date falls on the given calendar day " +
                "(both `2026-10-25` and `2026-10-25T14:30:00Z` match `date=2026-10-25`). " +
                "Ordered by operation timestamp, then id. Pages are numbered from ${PurchaseRequestService.FIRST_PAGE} and translated to " +
                "`LIMIT pageSize OFFSET (page - ${PurchaseRequestService.FIRST_PAGE}) * pageSize`."
    )
    @ApiResponse(responseCode = "200", description = "Page of purchase operations; empty when the date has no records or the page is past the end")
    @ApiResponse(responseCode = "400", description = "Invalid date, page or page size; `message` lists every problem", content = [Content()])
    @ResponseBody
    @GetMapping("/v1/get-all-purchases")
    fun getAllPurchases(
        @Parameter(description = "Calendar date in ISO 8601 format", example = "2026-10-25", required = true)
        @RequestParam("date") date: String,
        @Parameter(description = "Page number, starting from ${PurchaseRequestService.FIRST_PAGE}")
        @Schema(type = "integer", format = "int32", minimum = PurchaseRequestService.FIRST_PAGE.toString())
        @RequestParam("page", defaultValue = PurchaseRequestService.FIRST_PAGE.toString()) page: Int,
        @Parameter(description = "Records per page")
        @Schema(type = "integer", format = "int32", minimum = "1", maximum = PurchaseRequestService.MAX_PAGE_SIZE.toString())
        @RequestParam("pageSize", defaultValue = PurchaseRequestService.DEFAULT_PAGE_SIZE.toString()) pageSize: Int,
    ): List<PurchaseOperationDto> {
        logger.info("Handling get all purchases request for date $date (page $page, page size $pageSize)")

        val result = purchaseRequestService.getPurchaseOperationsByDate(date, page, pageSize)

        return result.getOrElse { error ->
            logger.warn("Get all purchases request rejected: ${error.message}")
            throw error.toResponseStatusException()
        }
    }

    @Operation(
        summary = "Record a purchase operation",
        description = "Rules: `date` is ISO 8601 (date or date-time), `quantity` and `boughtPrice` are positive, " +
                "and `itemId`, `quantityUnitId`, `packageId`, `currency` must reference existing records."
    )
    @ApiResponse(responseCode = "200", description = "`true` if saved, `false` if a referenced id does not exist or the database failed")
    @ApiResponse(responseCode = "400", description = "Validation failed; `message` lists every problem", content = [Content()])
    @ResponseBody
    @PostMapping("/v1/add-purchase-operation")
    fun addItemToStorage(@RequestBody purchaseRequest: PurchaseRequestDto): Boolean {
        logger.info("Handling purchase request $purchaseRequest")

        val purchaseRequestDomain = purchaseRequestService.convertToPurchaseRequest(purchaseRequest)
        val result = purchaseRequestService.savePurchase(purchaseRequestDomain)

        val error = result.exceptionOrNull()
        if (error is IllegalArgumentException) {
            logger.warn("Purchase request rejected: ${error.message}")
            throw error.toResponseStatusException()
        }

        return result.isSuccess
    }
}
