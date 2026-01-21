package com.bianca.moneymind.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        val currentMonth = YearMonth.now()
        val startOfMonth = currentMonth.atDay(1)
        val endOfMonth = currentMonth.atEndOfMonth()

        viewModelScope.launch {
            getTransactionsUseCase.byDateRange(startOfMonth, endOfMonth)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { transactions ->
                    val today = LocalDate.now()

                    // Calculate monthly totals
                    val monthlyExpense = transactions
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { it.amount }

                    val monthlyIncome = transactions
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }

                    // Group transactions by date (recent 7 days)
                    val recentDates = (0..6).map { today.minusDays(it.toLong()) }
                    val recentTransactions = transactions
                        .filter { it.date in recentDates }
                        .groupBy { it.date }
                        .toSortedMap(reverseOrder())

                    // Today's transactions
                    val todayTransactions = transactions.filter { it.date == today }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentMonth = currentMonth,
                            monthlyExpense = monthlyExpense,
                            monthlyIncome = monthlyIncome,
                            todayTransactions = todayTransactions,
                            recentTransactions = recentTransactions
                        )
                    }
                }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadTransactions()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
