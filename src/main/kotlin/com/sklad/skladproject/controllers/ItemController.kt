package com.sklad.skladproject.controllers

import com.sklad.skladproject.dto.ItemRequestDto
import com.sklad.skladproject.services.ItemService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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
}
