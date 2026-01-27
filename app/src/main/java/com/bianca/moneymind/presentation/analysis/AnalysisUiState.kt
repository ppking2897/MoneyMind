package com.bianca.moneymind.presentation.analysis

import com.bianca.moneymind.presentation.analysis.components.DailyExpenseData
import com.bianca.moneymind.presentation.analysis.components.MonthlyTrendData
import java.time.LocalDate
import java.time.YearMonth

enum class TimeRange {
    MONTH, YEAR
}

data class CategoryAmount(
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
    val selectedYear: Int = java.time.Year.now().value,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val categoryExpenses: List<CategoryAmount> = emptyList(),
    val categoryIncomes: List<CategoryAmount> = emptyList(),
    val dailyExpenses: List<DailyExpenseData> = emptyList(),
    val dailyIncomes: List<DailyExpenseData> = emptyList(),
    val monthlyTrend: List<MonthlyTrendData> = emptyList()
) {
    val balance: Double
        get() = totalIncome - totalExpense

    val isPositiveBalance: Boolean
        get() = balance >= 0

    companion object {
        fun mock(): AnalysisUiState {
            val today = LocalDate.now()
            return AnalysisUiState(
                isLoading = false,
                selectedTimeRange = TimeRange.MONTH,
                selectedMonth = YearMonth.now(),
                selectedYear = java.time.Year.now().value,
                totalExpense = 12350.0,
                totalIncome = 50000.0,
                categoryExpenses = listOf(
                    CategoryAmount("expense_food", "餐飲", 4500.0, 36.4f),
                    CategoryAmount("expense_transport", "交通", 3200.0, 25.9f),
                    CategoryAmount("expense_shopping", "購物", 2800.0, 22.7f),
                    CategoryAmount("expense_entertainment", "娛樂", 1850.0, 15.0f)
                ),
                categoryIncomes = listOf(
                    CategoryAmount("income_salary", "薪資", 45000.0, 90.0f),
                    CategoryAmount("income_bonus", "獎金", 5000.0, 10.0f)
                ),
                dailyExpenses = listOf(
                    DailyExpenseData(today.minusDays(6), 850.0),
                    DailyExpenseData(today.minusDays(5), 1200.0),
                    DailyExpenseData(today.minusDays(4), 650.0),
                    DailyExpenseData(today.minusDays(3), 1500.0),
                    DailyExpenseData(today.minusDays(2), 900.0),
                    DailyExpenseData(today.minusDays(1), 1100.0),
                    DailyExpenseData(today, 750.0)
                ),
                dailyIncomes = listOf(
                    DailyExpenseData(today.minusDays(6), 0.0),
                    DailyExpenseData(today.minusDays(5), 0.0),
                    DailyExpenseData(today.minusDays(4), 0.0),
                    DailyExpenseData(today.minusDays(3), 0.0),
                    DailyExpenseData(today.minusDays(2), 0.0),
                    DailyExpenseData(today.minusDays(1), 50000.0),
                    DailyExpenseData(today, 0.0)
                ),
                monthlyTrend = listOf(
                    MonthlyTrendData(YearMonth.now().minusMonths(5), 18000.0, 45000.0),
                    MonthlyTrendData(YearMonth.now().minusMonths(4), 15000.0, 45000.0),
                    MonthlyTrendData(YearMonth.now().minusMonths(3), 22000.0, 48000.0),
                    MonthlyTrendData(YearMonth.now().minusMonths(2), 19000.0, 45000.0),
                    MonthlyTrendData(YearMonth.now().minusMonths(1), 16500.0, 45000.0),
                    MonthlyTrendData(YearMonth.now(), 12350.0, 50000.0)
                )
            )
        }
    }
}
