package com.sklad.skladproject.services

import com.sklad.skladproject.domain.PurchaseRequest
import com.sklad.skladproject.repository.database.storage.DatabasePurchaseOperationHistoryRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
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

    @Test
    fun `all problems are reported together`() {
        val result = service.validatePurchaseRequest(
            validRequest.copy(date = "25.10.2026", quantity = 0f, boughtPrice = -1f)
        )

        val message = result.exceptionOrNull()!!.message!!
        assertEquals(3, message.split("; ").size, "expected three errors in: $message")
    }

    @Test
    fun `invalid request never reaches the database`() {
        val result = service.savePurchase(validRequest.copy(quantity = -1f))

        assertTrue(result.isFailure)
        verifyNoInteractions(repository)
    }

    @Test
    fun `purchase operations query with valid parameters passes`() {
        assertTrue(service.validatePurchaseOperationsQuery("2026-10-25", PurchaseRequestService.FIRST_PAGE, 50).isSuccess)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "25.10.2026", "2026-10-25T14:30:00Z", "2026-02-30"])
    fun `purchase operations query rejects non calendar dates`(date: String) {
        val result = service.validatePurchaseOperationsQuery(date, PurchaseRequestService.FIRST_PAGE, 50)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Date '$date' is not valid"))
    }

    @Test
    fun `purchase operations query rejects page before the first one`() {
        assertTrue(service.validatePurchaseOperationsQuery("2026-10-25", PurchaseRequestService.FIRST_PAGE - 1, 50).isFailure)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1, 501])
    fun `purchase operations query rejects bad page size`(pageSize: Int) {
        assertTrue(service.validatePurchaseOperationsQuery("2026-10-25", PurchaseRequestService.FIRST_PAGE, pageSize).isFailure)
    }

    @Test
    fun `page is converted to limit and offset`() {
        `when`(repository.getPurchaseOperationsByDate(anyString(), anyInt(), anyInt())).thenReturn(emptyList())
        val first = PurchaseRequestService.FIRST_PAGE

        service.getPurchaseOperationsByDate("2026-10-25", first, 50)
        verify(repository).getPurchaseOperationsByDate("2026-10-25", 50, 0)

        service.getPurchaseOperationsByDate("2026-10-25", first + 2, 50)
        verify(repository).getPurchaseOperationsByDate("2026-10-25", 50, 100)

        service.getPurchaseOperationsByDate("2026-10-25", first + 1, 20)
        verify(repository).getPurchaseOperationsByDate("2026-10-25", 20, 20)
    }

    @Test
    fun `invalid purchase operations query never reaches the database`() {
        val result = service.getPurchaseOperationsByDate("bad-date", PurchaseRequestService.FIRST_PAGE, 50)

        assertTrue(result.isFailure)
        verifyNoInteractions(repository)
    }
}
