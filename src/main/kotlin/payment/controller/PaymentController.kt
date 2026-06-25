package com.example.payment.controller

import com.example.payment.service.PaymentService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.math.BigDecimal
import java.util.UUID

class PaymentController(
    private val service: PaymentService
) {
    suspend fun listPayments(call: ApplicationCall) {
        val payments = service.listPayments()
        if (payments.isEmpty()) {
            call.respondText("No payments found", status = HttpStatusCode.OK)
            return
        }

        val response = payments.joinToString(separator = "\n") { payment ->
            "id=${payment.id}, accountId=${payment.accountId}, amount=${payment.amount}, reference=${payment.reference}, status=${payment.status}"
        }
        call.respondText(response, status = HttpStatusCode.OK)
    }

    suspend fun getPayment(call: ApplicationCall) {
        val idParam = call.parameters["id"]
        val id = try {
            UUID.fromString(idParam ?: "")
        } catch (ex: IllegalArgumentException) {
            call.respondText("Missing or invalid payment id", status = HttpStatusCode.BadRequest)
            return
        }

        val payment = service.getPayment(id)
        if (payment == null) {
            call.respondText("Payment not found", status = HttpStatusCode.NotFound)
            return
        }

        call.respondText(
            "id=${payment.id}, accountId=${payment.accountId}, amount=${payment.amount}, reference=${payment.reference}, status=${payment.status}",
            status = HttpStatusCode.OK
        )
    }

    suspend fun createPayment(call: ApplicationCall) {
        val accountIdText = call.request.queryParameters["accountId"]
        val amountText = call.request.queryParameters["amount"]
        val reference = call.request.queryParameters["reference"]

        val accountId = try {
            UUID.fromString(accountIdText ?: "")
        } catch (ex: IllegalArgumentException) {
            call.respondText("Missing or invalid accountId", status = HttpStatusCode.BadRequest)
            return
        }

        val amount = try {
            BigDecimal(amountText ?: "")
        } catch (ex: NumberFormatException) {
            call.respondText("Missing or invalid amount", status = HttpStatusCode.BadRequest)
            return
        }

        if (reference.isNullOrBlank()) {
            call.respondText("Missing payment reference", status = HttpStatusCode.BadRequest)
            return
        }

        val payment = service.createPayment(accountId, amount, reference)
        call.respondText(
            "Payment created: id=${payment.id}, status=${payment.status}",
            status = HttpStatusCode.Created
        )
    }
}
