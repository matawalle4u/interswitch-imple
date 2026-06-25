package com.example.ledger.service

import com.example.gateway.GatewayPolicy
import com.example.gateway.TransientGatewayException
import com.example.ledger.model.LedgerEntry
import com.example.ledger.repository.InMemoryLedgerRepository
import java.math.BigDecimal
import java.util.UUID

class LedgerService(
    private val policy: GatewayPolicy
) {
    private val repository = InMemoryLedgerRepository()

    fun postEntry(paymentId: UUID, accountId: UUID, amount: BigDecimal, reference: String, idempotencyKey: String): LedgerEntry =
        policy.execute(isIdempotent = true) {
            val entry = LedgerEntry(
                id = UUID.randomUUID(),
                paymentId = paymentId,
                accountId = accountId,
                amount = amount,
                reference = reference
            )
            repository.save(entry)
            entry
        }
}
