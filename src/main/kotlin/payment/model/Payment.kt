package com.example.payment.model

import com.example.serialization.BigDecimalAsStringSerializer
import com.example.serialization.InstantSerializer
import com.example.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Serializable
data class PaymentChargeRequest(
    @Serializable(with = UUIDSerializer::class)
    val accountId: UUID,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val amount: BigDecimal,
    val reference: String,
    val idempotencyKey: String
)

@Serializable
data class PaymentResponse(
    @Serializable(with = UUIDSerializer::class)
    val paymentId: UUID,
    val status: PaymentStatus,
    val message: String
)

@Serializable
data class PaymentTransaction(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val accountId: UUID,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val amount: BigDecimal,
    val reference: String,
    val idempotencyKey: String,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now(),
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant = Instant.now(),
    val failureReason: String? = null,
    val status: PaymentStatus = PaymentStatus.PENDING
)

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    RECONCILIATION_PENDING
}
