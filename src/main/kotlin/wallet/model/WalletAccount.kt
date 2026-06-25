package com.example.wallet.model

import com.example.serialization.BigDecimalAsStringSerializer
import com.example.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.UUID

@Serializable
data class WalletAccount(
    @Serializable(with = UUIDSerializer::class)
    val accountId: UUID,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val availableBalance: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val reservedBalance: BigDecimal = BigDecimal.ZERO
)
