package com.bianca.moneymind.domain.usecase

import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return transactionRepository.getAll()
    }

    fun byDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        return transactionRepository.getByDateRange(startDate, endDate)
    }

    fun byDate(date: LocalDate): Flow<List<Transaction>> {
        return transactionRepository.getByDate(date)
    }

    fun byCategory(categoryId: String): Flow<List<Transaction>> {
        return transactionRepository.getByCategory(categoryId)
    }
}
