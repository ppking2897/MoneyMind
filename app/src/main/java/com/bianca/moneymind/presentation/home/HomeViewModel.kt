package com.bianca.moneymind.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.usecase.GetCategoriesUseCase
import com.bianca.moneymind.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
class HomeViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadTransactions()
    }

    /**
     * Select a different time period
     */
    fun onPeriodSelected(period: TimePeriod) {
        if (period == _uiState.value.selectedPeriod) return

        _uiState.update { it.copy(selectedPeriod = period, isLoading = true) }
        loadTransactions()
    }

    private fun loadTransactions() {
        loadJob?.cancel()

        val period = _uiState.value.selectedPeriod
        val (startDate, endDate) = period.getDateRange()

        loadJob = viewModelScope.launch {
            combine(
                getTransactionsUseCase.byDateRange(startDate, endDate),
                getCategoriesUseCase()
            ) { transactions, categories ->
                val categoryMap = categories.associateBy { it.id }

                // Calculate totals for the period
                val periodExpense = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                val periodIncome = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }

                // Group transactions with category by date
                val transactionsWithCategory = transactions.map { tx ->
                    TransactionWithCategory(tx, categoryMap[tx.categoryId])
                }.groupBy { it.transaction.date }
                    .toSortedMap(reverseOrder())

                Triple(periodExpense, periodIncome, transactionsWithCategory)
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { (periodExpense, periodIncome, transactionsWithCategory) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentMonth = YearMonth.now(),
                            periodExpense = periodExpense,
                            periodIncome = periodIncome,
                            transactionsWithCategory = transactionsWithCategory
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
