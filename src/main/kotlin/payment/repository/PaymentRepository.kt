package com.example.payment.repository

import com.example.payment.model.Payment
import java.util.UUID

interface PaymentRepository {
    fun save(payment: Payment)
    fun findById(id: UUID): Payment?
    fun findAll(): List<Payment>
}

class InMemoryPaymentRepository : PaymentRepository {
    private val storage = mutableMapOf<UUID, Payment>()
    private val lock = Any()

    override fun save(payment: Payment) {
        synchronized(lock) {
            storage[payment.id] = payment
        }
    }

    override fun findById(id: UUID): Payment? = synchronized(lock) {
        storage[id]
    }

    override fun findAll(): List<Payment> = synchronized(lock) {
        storage.values.toList()
    }
}
