package com.example.ledger.repository

import com.example.ledger.model.LedgerEntry
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface LedgerRepository {
    fun save(entry: LedgerEntry)
    fun findAll(): List<LedgerEntry>
}

class InMemoryLedgerRepository : LedgerRepository {
    private val entries = ConcurrentHashMap<UUID, LedgerEntry>()

    override fun save(entry: LedgerEntry) {
        entries[entry.id] = entry
    }

    override fun findAll(): List<LedgerEntry> = entries.values.toList()
}
