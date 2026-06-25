package com.example

import com.example.payment.model.PaymentChargeRequest
import io.ktor.client.call.bodyAsText
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ServerTest {
    @Test
    fun `test root endpoint`() = testApplication {
        configure()
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

    @Test
    fun `test payment charge route`() = testApplication {
        configure()

        val request = PaymentChargeRequest(
            accountId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
            amount = java.math.BigDecimal("10.50"),
            reference = "test-charge",
            idempotencyKey = "idempotency-001"
        )

        val response = client.post("/payments/charge") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("paymentId"))
        assertTrue(bodyText.contains("COMPLETED"))
    }
}
