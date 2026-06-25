package com.example.payment.model

import java.math.BigDecimal
import java.util.UUID

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
}

data class Payment(
    val id: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val reference: String,
    val status: PaymentStatus
)
