package com.bianca.moneymind.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.usecase.GetCategoriesUseCase
import com.bianca.moneymind.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val month = _uiState.value.selectedMonth
        val startDate = month.atDay(1)
        val endDate = month.atEndOfMonth()

        viewModelScope.launch {
            combine(
                getTransactionsUseCase.byDateRange(startDate, endDate),
                getCategoriesUseCase()
            ) { transactions, categories ->
                val categoryMap = categories.associateBy { it.id }

                val totalExpense = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                val totalIncome = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }

                // Group expenses by category
                val expensesByCategory = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .map { (categoryId, txns) ->
                        val amount = txns.sumOf { it.amount }
                        val percentage = if (totalExpense > 0) {
                            (amount / totalExpense * 100).toFloat()
                        } else 0f

                        CategoryExpense(
                            categoryId = categoryId ?: "uncategorized",
                            categoryName = categoryId?.let { categoryMap[it]?.name } ?: "未分類",
                            amount = amount,
                            percentage = percentage
                        )
                    }
                    .sortedByDescending { it.amount }

                AnalysisUiState(
                    isLoading = false,
                    selectedMonth = month,
                    totalExpense = totalExpense,
                    totalIncome = totalIncome,
                    categoryExpenses = expensesByCategory
                )
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun onTimeRangeChange(timeRange: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = timeRange, isLoading = true) }
        loadData()
    }

    fun onMonthChange(month: YearMonth) {
        _uiState.update { it.copy(selectedMonth = month, isLoading = true) }
        loadData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
