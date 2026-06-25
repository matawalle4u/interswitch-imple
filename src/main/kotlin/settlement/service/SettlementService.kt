package com.example.settlement.service

import com.example.gateway.GatewayPolicy
import com.example.settlement.model.SettlementRecord
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class SettlementService(
    private val policy: GatewayPolicy
) {
    private val settlements = ConcurrentHashMap<UUID, SettlementRecord>()

    fun settlePayment(paymentId: UUID, accountId: UUID, amount: BigDecimal, idempotencyKey: String): SettlementRecord =
        policy.execute(isIdempotent = true) {
            val record = SettlementRecord(
                id = UUID.randomUUID(),
                paymentId = paymentId,
                accountId = accountId,
                amount = amount
            )
            settlements[record.id] = record
            record
        }

    fun findAll(): List<SettlementRecord> = settlements.values.toList()
}
