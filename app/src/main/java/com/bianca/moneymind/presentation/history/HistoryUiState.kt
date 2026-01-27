package com.bianca.moneymind.presentation.history

import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.InputType
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.presentation.home.TransactionWithCategory
import java.time.Instant
import java.time.LocalDate

enum class TransactionFilter {
    ALL, EXPENSE, INCOME
}

data class DailyTransactions(
    val date: LocalDate,
    val transactionsWithCategory: List<TransactionWithCategory>,
    val dailyTotal: Double
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val dailyTransactions: List<DailyTransactions> = emptyList()
) {
    companion object {
        private val mockCategories = listOf(
            Category("expense_food", "餐飲", "restaurant", "#FF5722", TransactionType.EXPENSE, null, true, 1),
            Category("expense_transport", "交通", "directions_car", "#2196F3", TransactionType.EXPENSE, null, true, 2),
            Category("income_salary", "薪水", "work", "#4CAF50", TransactionType.INCOME, null, true, 1)
        )

        fun mock(): HistoryUiState {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val categoryMap = mockCategories.associateBy { it.id }

            val todayTransactions = listOf(
                TransactionWithCategory(
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
                    categoryMap["expense_food"]
                ),
                TransactionWithCategory(
                    Transaction(
                        id = "2",
                        type = TransactionType.EXPENSE,
                        amount = 150.0,
                        description = "捷運加值",
                        categoryId = "expense_transport",
                        date = today,
                        createdAt = Instant.now().minusSeconds(3600),
                        inputType = InputType.MANUAL,
                        receiptImagePath = null,
                        merchantName = null,
                        note = null,
                        rawInput = null
                    ),
                    categoryMap["expense_transport"]
                )
            )

            val yesterdayTransactions = listOf(
                TransactionWithCategory(
                    Transaction(
                        id = "3",
                        type = TransactionType.INCOME,
                        amount = 50000.0,
                        description = "薪水",
                        categoryId = "income_salary",
                        date = yesterday,
                        createdAt = Instant.now().minusSeconds(86400),
                        inputType = InputType.MANUAL,
                        receiptImagePath = null,
                        merchantName = null,
                        note = null,
                        rawInput = null
                    ),
                    categoryMap["income_salary"]
                )
            )

            return HistoryUiState(
                isLoading = false,
                dailyTransactions = listOf(
                    DailyTransactions(today, todayTransactions, -235.0),
                    DailyTransactions(yesterday, yesterdayTransactions, 50000.0)
                )
            )
        }
    }
}
