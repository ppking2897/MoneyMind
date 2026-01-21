package com.bianca.moneymind.presentation.home

import com.bianca.moneymind.domain.model.Transaction
import java.time.LocalDate
import java.time.YearMonth

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentMonth: YearMonth = YearMonth.now(),
    val monthlyExpense: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val budget: Double = 20000.0, // TODO: Load from settings
    val todayTransactions: List<Transaction> = emptyList(),
    val recentTransactions: Map<LocalDate, List<Transaction>> = emptyMap()
) {
    val budgetProgress: Float
        get() = if (budget > 0) (monthlyExpense / budget).toFloat().coerceIn(0f, 1f) else 0f

    val budgetPercentage: Int
        get() = (budgetProgress * 100).toInt()
}
