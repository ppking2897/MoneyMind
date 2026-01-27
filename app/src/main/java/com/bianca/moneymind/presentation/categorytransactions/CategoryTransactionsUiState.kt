package com.bianca.moneymind.presentation.categorytransactions

import com.bianca.moneymind.domain.model.InputType
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate

data class CategoryTransactionsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val categoryId: String = "",
    val categoryName: String = "",
    val transactions: Map<LocalDate, List<Transaction>> = emptyMap(),
    val totalAmount: Double = 0.0,
    val transactionCount: Int = 0
) {
    companion object {
        fun mock(): CategoryTransactionsUiState {
            val today = LocalDate.now()
            val mockTransactions = listOf(
                Transaction(
                    id = "1",
                    type = TransactionType.EXPENSE,
                    amount = 85.0,
                    description = "午餐便當",
                    categoryId = "expense_food",
                    date = today,
                    createdAt = Instant.now(),
                    inputType = InputType.MANUAL,
                    receiptImagePath = null,
                    merchantName = null,
                    note = null,
                    rawInput = null
                ),
                Transaction(
                    id = "2",
                    type = TransactionType.EXPENSE,
                    amount = 150.0,
                    description = "晚餐",
                    categoryId = "expense_food",
                    date = today,
                    createdAt = Instant.now().minusSeconds(3600),
                    inputType = InputType.MANUAL,
                    receiptImagePath = null,
                    merchantName = null,
                    note = null,
                    rawInput = null
                ),
                Transaction(
                    id = "3",
                    type = TransactionType.EXPENSE,
                    amount = 80.0,
                    description = "早餐",
                    categoryId = "expense_food",
                    date = today.minusDays(1),
                    createdAt = Instant.now().minusSeconds(86400),
                    inputType = InputType.MANUAL,
                    receiptImagePath = null,
                    merchantName = null,
                    note = null,
                    rawInput = null
                )
            )

            return CategoryTransactionsUiState(
                isLoading = false,
                categoryId = "expense_food",
                categoryName = "餐飲",
                transactions = mockTransactions.groupBy { it.date },
                totalAmount = 315.0,
                transactionCount = 3
            )
        }
    }
}
