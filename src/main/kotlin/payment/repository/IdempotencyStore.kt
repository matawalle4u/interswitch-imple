package com.example.payment.repository

import java.util.UUID

interface IdempotencyStore {
    fun reserve(idempotencyKey: String, transactionId: UUID): Boolean
    fun findTransactionId(idempotencyKey: String): UUID?
}

class InMemoryIdempotencyStore : IdempotencyStore {
    private val map = mutableMapOf<String, UUID>()

    override fun reserve(idempotencyKey: String, transactionId: UUID): Boolean {
        synchronized(map) {
            if (map.containsKey(idempotencyKey)) {
                return false
            }
            map[idempotencyKey] = transactionId
            return true
        }
    }

    override fun findTransactionId(idempotencyKey: String): UUID? = synchronized(map) {
        map[idempotencyKey]
    }
}
