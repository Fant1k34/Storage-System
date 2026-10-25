package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.ItemDto
import com.sklad.skladproject.dto.ItemRequestDto
import com.sklad.skladproject.services.ItemService
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
class ItemController(
    val itemService: ItemService
) {
    private val logger = LoggerFactory.getLogger("ItemController")

    @ResponseBody
    @PostMapping("/v1/add-item")
    fun addItem(@RequestBody itemRequest: ItemRequestDto): Boolean {
        logger.info("Handling add item request $itemRequest")

        val itemDomain = itemService.convertToItem(itemRequest)
        val result = itemService.saveItem(itemDomain)

        return result.isSuccess
    }

    @ResponseBody
    @GetMapping("/v1/get-all-items")
    fun getAllItems(): List<ItemDto> {
        logger.info("Handling get all items request")

        return itemService.getAllItems()
    }

    @ResponseBody
    @GetMapping("/v1/get-item")
    fun getItem(@RequestParam("name") name: String): ResponseEntity<ItemDto> {
        logger.info("Handling get item request for name $name")

        val item = itemService.getItemByName(name) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(item)
    }
}
