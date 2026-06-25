package com.example.payment.routes

import com.example.payment.controller.PaymentController
import io.ktor.server.routing.*

fun Route.paymentRoutes(controller: PaymentController) {
    route("/payments") {
        post("/charge") {
            controller.createCharge(call)
        }
        get {
            controller.listPayments(call)
        }
        get("/{id}") {
            controller.getPayment(call)
        }
        post("/reconcile") {
            controller.reconcilePending(call)
        }
    }
}
