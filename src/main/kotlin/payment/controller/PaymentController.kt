package com.example.payment.controller

import com.example.payment.model.PaymentChargeRequest
import com.example.payment.service.PaymentService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.UUID

class PaymentController(
    private val paymentService: PaymentService
) {
    suspend fun createCharge(call: ApplicationCall) {
        val request = call.receive<PaymentChargeRequest>()
        val response = paymentService.charge(request)
        val status = when (response.status) {
            com.example.payment.model.PaymentStatus.COMPLETED -> HttpStatusCode.Created
            com.example.payment.model.PaymentStatus.RECONCILIATION_PENDING -> HttpStatusCode.Accepted
            else -> HttpStatusCode.BadRequest
        }
        call.respond(status, response)
    }

    suspend fun listPayments(call: ApplicationCall) {
        call.respond(paymentService.listPayments())
    }

    suspend fun getPayment(call: ApplicationCall) {
        val idParam = call.parameters["id"] ?: return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id parameter"))
        val id = try {
            UUID.fromString(idParam)
        } catch (ex: IllegalArgumentException) {
            return call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Malformed id"))
        }

        val payment = paymentService.getPayment(id)
        if (payment == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Payment not found"))
            return
        }
        call.respond(payment)
    }

    suspend fun reconcilePending(call: ApplicationCall) {
        val results = paymentService.reconcilePending()
        call.respond(results)
    }
}
