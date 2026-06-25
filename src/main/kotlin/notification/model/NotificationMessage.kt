package com.example.notification.model

import com.example.serialization.InstantSerializer
import com.example.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class NotificationMessage(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val paymentId: UUID,
    val message: String,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now()
)
