package com.example.payment.repository

import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

interface ReconciliationQueue {
    fun enqueue(transactionId: UUID)
    fun poll(): UUID?
    fun pending(): List<UUID>
}

class InMemoryReconciliationQueue : ReconciliationQueue {
    private val queue = ConcurrentLinkedQueue<UUID>()

    override fun enqueue(transactionId: UUID) {
        queue.add(transactionId)
    }

    override fun poll(): UUID? = queue.poll()

    override fun pending(): List<UUID> = queue.toList()
}
