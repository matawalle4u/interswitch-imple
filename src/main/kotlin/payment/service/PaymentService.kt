package com.example.payment.service

import com.example.gateway.GatewayUnavailableException
import com.example.payment.model.PaymentChargeRequest
import com.example.payment.model.PaymentResponse
import com.example.payment.model.PaymentStatus
import com.example.payment.model.PaymentTransaction
import com.example.payment.repository.IdempotencyStore
import com.example.payment.repository.PaymentRepository
import com.example.payment.repository.ReconciliationQueue
import com.example.ledger.service.LedgerService
import com.example.notification.service.NotificationService
import com.example.settlement.service.SettlementService
import com.example.wallet.service.WalletService
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val idempotencyStore: IdempotencyStore,
    private val reconciliationQueue: ReconciliationQueue,
    private val walletService: WalletService,
    private val ledgerService: LedgerService,
    private val settlementService: SettlementService,
    private val notificationService: NotificationService
) {
    fun charge(request: PaymentChargeRequest): PaymentResponse {
        val existingPaymentId = idempotencyStore.findTransactionId(request.idempotencyKey)
        if (existingPaymentId != null) {
            val existing = paymentRepository.findById(existingPaymentId)
            return existing?.toResponse() ?: PaymentResponse(existingPaymentId, PaymentStatus.PENDING, "Processing")
        }

        val transaction = PaymentTransaction(
            id = UUID.randomUUID(),
            accountId = request.accountId,
            amount = request.amount,
            reference = request.reference,
            idempotencyKey = request.idempotencyKey,
            status = PaymentStatus.PENDING
        )

        paymentRepository.save(transaction)
        idempotencyStore.reserve(request.idempotencyKey, transaction.id)

        var reserveCompleted = false
        try {
            if (!walletService.validateAccount(request.accountId)) {
                throw IllegalArgumentException("Account not found: ${request.accountId}")
            }

            walletService.reserveFunds(request.accountId, request.amount, request.idempotencyKey)
            reserveCompleted = true
            ledgerService.postEntry(transaction.id, request.accountId, request.amount, request.reference, request.idempotencyKey)
            settlementService.settlePayment(transaction.id, request.accountId, request.amount, request.idempotencyKey)
            notificationService.notifyPayment(transaction.id, "Payment completed for ${request.reference}", request.idempotencyKey)

            val completed = transaction.copy(
                status = PaymentStatus.COMPLETED,
                updatedAt = Instant.now()
            )
            paymentRepository.update(completed)
            return completed.toResponse("Payment completed successfully")
        } catch (ex: Exception) {
            if (reserveCompleted) {
                runCatching { walletService.releaseFunds(request.accountId, request.amount, request.idempotencyKey) }
            }

            val status = if (ex is GatewayUnavailableException || ex is java.util.concurrent.TimeoutException) {
                PaymentStatus.RECONCILIATION_PENDING
            } else {
                PaymentStatus.FAILED
            }

            val broken = transaction.copy(
                status = status,
                updatedAt = Instant.now(),
                failureReason = ex.message
            )
            paymentRepository.update(broken)

            if (status == PaymentStatus.RECONCILIATION_PENDING) {
                reconciliationQueue.enqueue(transaction.id)
            }

            return PaymentResponse(transaction.id, status, ex.message ?: "Payment failed")
        }
    }

    fun getPayment(id: UUID): PaymentTransaction? = paymentRepository.findById(id)

    fun listPayments(): List<PaymentTransaction> = paymentRepository.findAll()

    fun reconcilePending(): List<PaymentResponse> {
        val results = mutableListOf<PaymentResponse>()
        while (true) {
            val nextId = reconciliationQueue.poll() ?: break
            val transaction = paymentRepository.findById(nextId) ?: continue
            if (transaction.status != PaymentStatus.RECONCILIATION_PENDING) {
                continue
            }

            val request = PaymentChargeRequest(
                accountId = transaction.accountId,
                amount = transaction.amount,
                reference = transaction.reference,
                idempotencyKey = transaction.idempotencyKey
            )
            val response = try {
                charge(request)
            } catch (ex: Exception) {
                PaymentResponse(transaction.id, transaction.status, "Reconciliation failed: ${ex.message}")
            }
            results.add(response)
        }
        return results
    }

    private fun PaymentTransaction.toResponse(message: String = status.name): PaymentResponse = PaymentResponse(
        paymentId = id,
        status = status,
        message = message
    )
}
