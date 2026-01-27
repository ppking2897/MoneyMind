package com.bianca.moneymind.domain.model

import java.time.LocalDate

/**
 * Domain model for a transaction parsed from natural language input
 */
data class ParsedTransaction(
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
 * Result containing multiple parsed transactions
 */
data class ParsedTransactionsResult(
    val transactions: List<ParsedTransaction>
) {
    val allComplete: Boolean
        get() = transactions.all { it.isComplete }

    val incompleteTransactions: List<ParsedTransaction>
        get() = transactions.filter { !it.isComplete }
}
