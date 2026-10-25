package com.sklad.skladproject.services

import com.sklad.skladproject.domain.PurchaseRequest
import com.sklad.skladproject.repository.database.storage.DatabasePurchaseOperationHistoryRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PurchaseRequestServiceValidationTest {
    private val repository = mock(DatabasePurchaseOperationHistoryRepository::class.java)
    private val service = PurchaseRequestService(repository)

    private val validRequest = PurchaseRequest(
        date = "2026-10-25",
        itemId = 1,
        quantity = 2.5f,
        quantityUnitId = 1,
        packageId = 1,
        packageAmount = 3,
        boughtPrice = 100f,
        currency = 1,
    )

    @Test
    fun `valid request passes`() {
        assertTrue(service.validatePurchaseRequest(validRequest).isSuccess)
    }

    @ParameterizedTest
    @ValueSource(floats = [0f, -0.01f, -5f, Float.NaN])
    fun `non positive quantity is rejected`(quantity: Float) {
        val result = service.validatePurchaseRequest(validRequest.copy(quantity = quantity))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Quantity must be positive"))
    }

    @ParameterizedTest
    @ValueSource(floats = [0f, -1f, Float.NaN])
    fun `non positive price is rejected`(price: Float) {
        val result = service.validatePurchaseRequest(validRequest.copy(boughtPrice = price))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Bought price must be positive"))
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun `non positive package amount is rejected`(amount: Int) {
        val result = service.validatePurchaseRequest(validRequest.copy(packageAmount = amount))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Package amount must be positive"))
    }

    @Test
    fun `all problems are reported together`() {
        val result = service.validatePurchaseRequest(
            validRequest.copy(date = "25.10.2026", quantity = 0f, boughtPrice = -1f, packageAmount = 0)
        )

        val message = result.exceptionOrNull()!!.message!!
        assertEquals(4, message.split("; ").size, "expected four errors in: $message")
    }

    @Test
    fun `invalid request never reaches the database`() {
        val result = service.savePurchase(validRequest.copy(quantity = -1f))

        assertTrue(result.isFailure)
        verifyNoInteractions(repository)
    }
}
