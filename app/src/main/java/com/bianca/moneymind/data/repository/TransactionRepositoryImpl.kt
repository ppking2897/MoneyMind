package com.bianca.moneymind.data.repository

import com.bianca.moneymind.data.local.dao.TransactionDao
import com.bianca.moneymind.data.local.mapper.toDomain
import com.bianca.moneymind.data.local.mapper.toDomainList
import com.bianca.moneymind.data.local.mapper.toEntity
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAll(): Flow<List<Transaction>> {
        return transactionDao.getAll().map { it.toDomainList() }
    }

    override fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        return transactionDao.getByDateRange(
            startDate.toEpochDay(),
            endDate.toEpochDay()
        ).map { it.toDomainList() }
    }

    override fun getByCategory(categoryId: String): Flow<List<Transaction>> {
        return transactionDao.getByCategory(categoryId).map { it.toDomainList() }
    }

    override fun getByDate(date: LocalDate): Flow<List<Transaction>> {
        return transactionDao.getByDate(date.toEpochDay()).map { it.toDomainList() }
    }

    override suspend fun getById(id: String): Transaction? {
        return transactionDao.getById(id)?.toDomain()
    }

    override suspend fun add(transaction: Transaction) {
        transactionDao.insert(transaction.toEntity())
    }

    override suspend fun addAll(transactions: List<Transaction>) {
        transactionDao.insertAll(transactions.map { it.toEntity() })
    }

    override suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction.toEntity())
    }

    override suspend fun delete(id: String) {
        transactionDao.delete(id)
    }
}
