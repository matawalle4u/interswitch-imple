package com.example

import com.example.payment.controller.PaymentController
import com.example.payment.repository.InMemoryPaymentRepository
import com.example.payment.routes.paymentRoutes
import com.example.payment.service.PaymentService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val paymentController = PaymentController(
        PaymentService(InMemoryPaymentRepository())
    )

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }

        paymentRoutes(paymentController)
    }
}
