package com.bianca.moneymind.domain.usecase

import com.bianca.moneymind.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: String) {
        transactionRepository.delete(transactionId)
    }
}
