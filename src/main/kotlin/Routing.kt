package com.example

import com.example.gateway.GatewayPolicy
import com.example.notification.service.NotificationService
import com.example.payment.controller.PaymentController
import com.example.payment.repository.InMemoryPaymentRepository
import com.example.payment.repository.InMemoryIdempotencyStore
import com.example.payment.repository.InMemoryReconciliationQueue
import com.example.payment.routes.paymentRoutes
import com.example.payment.service.PaymentService
import com.example.settlement.service.SettlementService
import com.example.wallet.repository.InMemoryWalletRepository
import com.example.wallet.service.WalletService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                io.ktor.http.HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unexpected error"))
            )
        }
    }

    val walletRepository = InMemoryWalletRepository()
    val walletService = WalletService(walletRepository, GatewayPolicy("wallet-policy"))
    val paymentRepository = InMemoryPaymentRepository()
    val idempotencyStore = InMemoryIdempotencyStore()
    val reconciliationQueue = InMemoryReconciliationQueue()
    val ledgerService = com.example.ledger.service.LedgerService(GatewayPolicy("ledger-policy"))
    val settlementService = SettlementService(GatewayPolicy("settlement-policy"))
    val notificationService = NotificationService(GatewayPolicy("notification-policy"))
    val paymentService = PaymentService(
        paymentRepository,
        idempotencyStore,
        reconciliationQueue,
        walletService,
        ledgerService,
        settlementService,
        notificationService
    )
    val paymentController = PaymentController(paymentService)

    routing {
        get("/") {
            call.respondText("Interswitch payment saga mock API")
        }
        paymentRoutes(paymentController)
    }
}
