package com.example

import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ServerTest {

    @Test
    fun `test root endpoint`() = testApplication {
        configure()
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

    @Test
    fun `test payments endpoints`() = testApplication {
        configure()

        val accountId = "00000000-0000-0000-0000-000000000001"
        val createResponse = client.post("/payments/create?accountId=$accountId&amount=10.50&reference=test")
        assertEquals(HttpStatusCode.Created, createResponse.status)

        val listResponse = client.get("/payments")
        assertEquals(HttpStatusCode.OK, listResponse.status)
        assertTrue(listResponse.bodyAsText().contains("reference=test"))
    }
}
