package com.bianca.moneymind.data.remote.gemini

import com.bianca.moneymind.domain.model.TransactionType
import java.time.LocalDate

/**
 * DTO for parsed transaction from Gemini API response
 */
data class ParsedTransactionDto(
    val type: TransactionType,
    val amount: Double?,
    val date: LocalDate,
    val merchantName: String?,
    val categoryId: String?,
    val description: String,
    val missingFields: List<String>,
    val confidence: Float
) {
    val isComplete: Boolean
        get() = missingFields.isEmpty() && amount != null

    companion object {
        val REQUIRED_FIELDS = listOf("amount")
    }
}

/**
 * Response from Gemini containing multiple parsed transactions
 */
data class ParsedTransactionsResponse(
    val transactions: List<ParsedTransactionDto>
) {
    val allComplete: Boolean
        get() = transactions.all { it.isComplete }

    val incompleteTransactions: List<ParsedTransactionDto>
        get() = transactions.filter { !it.isComplete }
}
