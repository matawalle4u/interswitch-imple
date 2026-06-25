package com.example.payment.service

import com.example.payment.model.Payment
import com.example.payment.model.PaymentStatus
import com.example.payment.repository.PaymentRepository
import java.math.BigDecimal
import java.util.UUID

class PaymentService(
    private val repository: PaymentRepository
) {
    fun createPayment(accountId: UUID, amount: BigDecimal, reference: String): Payment {
        val payment = Payment(
            id = UUID.randomUUID(),
            accountId = accountId,
            amount = amount,
            reference = reference,
            status = PaymentStatus.PENDING
        )
        repository.save(payment)
        return payment
    }

    fun getPayment(id: UUID): Payment? = repository.findById(id)

    fun listPayments(): List<Payment> = repository.findAll()
}
