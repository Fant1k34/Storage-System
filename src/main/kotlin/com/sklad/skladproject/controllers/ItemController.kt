package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.ItemDto
import com.sklad.skladproject.dto.ItemRequestDto
import com.sklad.skladproject.services.ItemService
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

@Tag(name = "Items", description = "Items that can be purchased and stored")
@RestController
@RequestMapping("/")
class ItemController(
    val itemService: ItemService
) {
    private val logger = LoggerFactory.getLogger("ItemController")

    @Operation(summary = "Add an item", description = "Idempotent: an existing an item is not duplicated.")
    @ApiResponse(responseCode = "200", description = "`true` if the record exists after the call (created or already present), `false` on database failure")
    @ResponseBody
    @PostMapping("/v1/add-item")
    fun addItem(@RequestBody itemRequest: ItemRequestDto): Boolean {
        logger.info("Handling add item request $itemRequest")

        val itemDomain = itemService.convertToItem(itemRequest)
        val result = itemService.saveItem(itemDomain)

        return result.isSuccess
    }

    @Operation(summary = "List all items", description = "Ordered by id.")
    @ResponseBody
    @GetMapping("/v1/get-all-items")
    fun getAllItems(): List<ItemDto> {
        logger.info("Handling get all items request")

        return itemService.getAllItems()
    }

    @Operation(summary = "Get an item by name")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "404", description = "No record with this name", content = [Content()])
    @ResponseBody
    @GetMapping("/v1/get-item")
    fun getItem(@Parameter(description = "Exact name to look up", example = "apple") @RequestParam("name") name: String): ResponseEntity<ItemDto> {
        logger.info("Handling get item request for name $name")

        val item = itemService.getItemByName(name) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(item)
    }
}
