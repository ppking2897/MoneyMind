package com.bianca.moneymind.presentation.analysis

import java.time.YearMonth

enum class TimeRange {
    DAY, WEEK, MONTH, YEAR
}

data class CategoryExpense(
    val categoryId: String,
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedTimeRange: TimeRange = TimeRange.MONTH,
    val selectedMonth: YearMonth = YearMonth.now(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val categoryExpenses: List<CategoryExpense> = emptyList()
) {
    val balance: Double
        get() = totalIncome - totalExpense

    val isPositiveBalance: Boolean
        get() = balance >= 0
}
