package com.bianca.moneymind.domain.usecase

import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        transactionRepository.add(transaction)
    }
}
