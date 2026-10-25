package com.sklad.skladproject.services

import com.sklad.skladproject.dto.ItemRequestDto
import com.sklad.skladproject.repository.database.storage.DatabaseItemRepository
import org.springframework.stereotype.Service

@Service
class ItemService(
    val databaseItemRepository: DatabaseItemRepository
) {
    fun convertToItem(itemRequestDto: ItemRequestDto): String {
        return itemRequestDto.name
    }

    fun saveItem(item: String): Result<Long> {
        return databaseItemRepository.trySaveItem(item)
    }
}
