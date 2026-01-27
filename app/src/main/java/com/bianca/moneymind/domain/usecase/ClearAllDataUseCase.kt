package com.bianca.moneymind.domain.usecase

import com.bianca.moneymind.domain.repository.TransactionRepository
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke() {
        transactionRepository.deleteAll()
    }
}
