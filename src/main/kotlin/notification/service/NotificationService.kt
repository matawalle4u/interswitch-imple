package com.example.notification.service

import com.example.gateway.GatewayPolicy
import com.example.notification.model.NotificationMessage
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class NotificationService(
    private val policy: GatewayPolicy
) {
    private val notifications = ConcurrentHashMap<UUID, NotificationMessage>()

    fun notifyPayment(paymentId: UUID, message: String, idempotencyKey: String): NotificationMessage =
        policy.execute(isIdempotent = true) {
            val notification = NotificationMessage(
                id = UUID.randomUUID(),
                paymentId = paymentId,
                message = message
            )
            notifications[notification.id] = notification
            notification
        }

    fun findAll(): List<NotificationMessage> = notifications.values.toList()
}
