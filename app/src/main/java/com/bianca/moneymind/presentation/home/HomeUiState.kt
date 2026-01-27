package com.bianca.moneymind.presentation.home

import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.InputType
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * Transaction with its associated category info
 */
data class TransactionWithCategory(
    val transaction: Transaction,
    val category: Category?
)

/**
 * Time periods for filtering transactions
 */
enum class TimePeriod(val displayName: String) {
    THIS_MONTH("本月"),
    LAST_MONTH("上月"),
    THIS_WEEK("本週"),
    TODAY("今日");

    fun getDateRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (this) {
            THIS_MONTH -> {
                val start = today.withDayOfMonth(1)
                val end = today.with(TemporalAdjusters.lastDayOfMonth())
                start to end
            }
            LAST_MONTH -> {
                val lastMonth = today.minusMonths(1)
                val start = lastMonth.withDayOfMonth(1)
                val end = lastMonth.with(TemporalAdjusters.lastDayOfMonth())
                start to end
            }
            THIS_WEEK -> {
                val start = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                val end = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
                start to end
            }
            TODAY -> {
                today to today
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedPeriod: TimePeriod = TimePeriod.THIS_MONTH,
    val currentMonth: YearMonth = YearMonth.now(),
    val periodExpense: Double = 0.0,
    val periodIncome: Double = 0.0,
    val budget: Double = 20000.0, // TODO: Load from settings
    val transactionsWithCategory: Map<LocalDate, List<TransactionWithCategory>> = emptyMap()
) {
    val budgetProgress: Float
        get() = if (budget > 0) (periodExpense / budget).toFloat().coerceIn(0f, 1f) else 0f

    val budgetPercentage: Int
        get() = (budgetProgress * 100).toInt()

    val periodLabel: String
        get() = selectedPeriod.displayName

    companion object {
        private val mockCategories = listOf(
            Category("expense_food", "餐飲", "restaurant", "#FF5722", TransactionType.EXPENSE, null, true, 1),
            Category("expense_transport", "交通", "directions_car", "#2196F3", TransactionType.EXPENSE, null, true, 2),
            Category("income_salary", "薪水", "work", "#4CAF50", TransactionType.INCOME, null, true, 1)
        )

        fun mock(): HomeUiState {
            val today = LocalDate.now()
            val categoryMap = mockCategories.associateBy { it.id }

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
                Transaction(
                    id = "3",
                    type = TransactionType.INCOME,
                    amount = 50000.0,
                    description = "薪水",
                    categoryId = "income_salary",
                    date = today.minusDays(1),
                    createdAt = Instant.now().minusSeconds(86400),
                    inputType = InputType.MANUAL,
                    receiptImagePath = null,
                    merchantName = null,
                    note = null,
                    rawInput = null
                )
            )

            val transactionsWithCategory = mockTransactions.map { tx ->
                TransactionWithCategory(tx, categoryMap[tx.categoryId])
            }.groupBy { it.transaction.date }

            return HomeUiState(
                isLoading = false,
                selectedPeriod = TimePeriod.THIS_MONTH,
                currentMonth = YearMonth.now(),
                periodExpense = 12350.0,
                periodIncome = 50000.0,
                budget = 20000.0,
                transactionsWithCategory = transactionsWithCategory
            )
        }
    }
}
