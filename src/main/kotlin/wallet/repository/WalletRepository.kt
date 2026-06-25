package com.example.wallet.repository

import com.example.wallet.model.WalletAccount
import java.math.BigDecimal
import java.util.UUID

interface WalletRepository {
    fun find(accountId: UUID): WalletAccount?
    fun save(account: WalletAccount)
}

class InMemoryWalletRepository : WalletRepository {
    private val accounts = mutableMapOf<UUID, WalletAccount>()

    init {
        // Seed a test wallet account to exercise the mock services.
        val seedId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        accounts[seedId] = WalletAccount(seedId, BigDecimal("1000.00"), BigDecimal.ZERO)
    }

    override fun find(accountId: UUID): WalletAccount? = synchronized(accounts) {
        accounts[accountId]
    }

    override fun save(account: WalletAccount) {
        synchronized(accounts) {
            accounts[account.accountId] = account
        }
    }
}
