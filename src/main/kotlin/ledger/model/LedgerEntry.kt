package com.example.ledger.model

import com.example.serialization.BigDecimalAsStringSerializer
import com.example.serialization.InstantSerializer
import com.example.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Serializable
data class LedgerEntry(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val paymentId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val accountId: UUID,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val amount: BigDecimal,
    val reference: String,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now()
)
