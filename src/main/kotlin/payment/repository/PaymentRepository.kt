package com.example.payment.repository

import com.example.payment.model.PaymentTransaction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface PaymentRepository {
    fun save(transaction: PaymentTransaction)
    fun update(transaction: PaymentTransaction)
    fun findById(id: UUID): PaymentTransaction?
    fun findAll(): List<PaymentTransaction>
}

class InMemoryPaymentRepository : PaymentRepository {
    private val storage = ConcurrentHashMap<UUID, PaymentTransaction>()

    override fun save(transaction: PaymentTransaction) {
        storage[transaction.id] = transaction
    }

    override fun update(transaction: PaymentTransaction) {
        storage[transaction.id] = transaction
    }

    override fun findById(id: UUID): PaymentTransaction? = storage[id]

    override fun findAll(): List<PaymentTransaction> = storage.values.toList()
}
