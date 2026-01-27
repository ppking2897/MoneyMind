package com.bianca.moneymind.domain.repository

import com.bianca.moneymind.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>
    fun getByCategory(categoryId: String): Flow<List<Transaction>>
    fun getByDate(date: LocalDate): Flow<List<Transaction>>
    suspend fun getById(id: String): Transaction?
    suspend fun add(transaction: Transaction)
    suspend fun addAll(transactions: List<Transaction>)
    suspend fun update(transaction: Transaction)
    suspend fun delete(id: String)
    suspend fun deleteAll()
}
