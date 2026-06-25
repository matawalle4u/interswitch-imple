package com.example.wallet.service

import com.example.gateway.GatewayPolicy
import com.example.gateway.TransientGatewayException
import com.example.wallet.model.WalletAccount
import com.example.wallet.repository.WalletRepository
import java.math.BigDecimal
import java.util.UUID

class WalletService(
    private val repository: WalletRepository,
    private val policy: GatewayPolicy
) {
    fun validateAccount(accountId: UUID): Boolean = policy.execute(isIdempotent = true) {
        repository.find(accountId) != null
    }

    fun reserveFunds(accountId: UUID, amount: BigDecimal, idempotencyKey: String): Boolean = policy.execute(isIdempotent = true) {
        val account = repository.find(accountId) ?: throw TransientGatewayException("Account not found for wallet reserve")
        if (account.availableBalance < amount) {
            throw IllegalStateException("Insufficient balance for account: $accountId")
        }
        val updated = account.copy(
            availableBalance = account.availableBalance - amount,
            reservedBalance = account.reservedBalance + amount
        )
        repository.save(updated)
        true
    }

    fun releaseFunds(accountId: UUID, amount: BigDecimal, idempotencyKey: String): Boolean = policy.execute(isIdempotent = true) {
        val account = repository.find(accountId) ?: throw TransientGatewayException("Account not found for wallet release")
        val released = account.reservedBalance.min(amount)
        val updated = account.copy(
            availableBalance = account.availableBalance + released,
            reservedBalance = account.reservedBalance - released
        )
        repository.save(updated)
        true
    }

    fun commitFunds(accountId: UUID, amount: BigDecimal, idempotencyKey: String): Boolean = policy.execute(isIdempotent = true) {
        val account = repository.find(accountId) ?: throw TransientGatewayException("Account not found for wallet commit")
        if (account.reservedBalance < amount) {
            throw IllegalStateException("Reserved funds are not sufficient for commit")
        }
        val updated = account.copy(
            reservedBalance = account.reservedBalance - amount
        )
        repository.save(updated)
        true
    }
}
