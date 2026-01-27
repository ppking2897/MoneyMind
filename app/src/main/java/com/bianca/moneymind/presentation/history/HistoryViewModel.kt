package com.bianca.moneymind.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.usecase.GetCategoriesUseCase
import com.bianca.moneymind.domain.usecase.GetTransactionsUseCase
import com.bianca.moneymind.presentation.home.TransactionWithCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            combine(
                getTransactionsUseCase(),
                getCategoriesUseCase()
            ) { transactions, categories ->
                val categoryMap = categories.associateBy { it.id }
                val filter = _uiState.value.selectedFilter
                val query = _uiState.value.searchQuery

                // Apply filter
                val filtered = transactions.filter { txn ->
                    val matchesFilter = when (filter) {
                        TransactionFilter.ALL -> true
                        TransactionFilter.EXPENSE -> txn.type == TransactionType.EXPENSE
                        TransactionFilter.INCOME -> txn.type == TransactionType.INCOME
                    }
                    val matchesQuery = query.isEmpty() ||
                            txn.description.contains(query, ignoreCase = true) ||
                            txn.merchantName?.contains(query, ignoreCase = true) == true

                    matchesFilter && matchesQuery
                }

                // Map to TransactionWithCategory and group by date
                val grouped = filtered
                    .map { txn -> TransactionWithCategory(txn, categoryMap[txn.categoryId]) }
                    .groupBy { it.transaction.date }
                    .map { (date, txnsWithCategory) ->
                        val dailyTotal = txnsWithCategory.sumOf { txnWithCategory ->
                            val txn = txnWithCategory.transaction
                            if (txn.type == TransactionType.EXPENSE) -txn.amount else txn.amount
                        }
                        DailyTransactions(
                            date = date,
                            transactionsWithCategory = txnsWithCategory.sortedByDescending { it.transaction.createdAt },
                            dailyTotal = dailyTotal
                        )
                    }
                    .sortedByDescending { it.date }

                grouped
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { grouped ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dailyTransactions = grouped
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadTransactions()
    }

    fun onFilterChange(filter: TransactionFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadTransactions()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            combine(
                getTransactionsUseCase(),
                getCategoriesUseCase()
            ) { transactions, categories ->
                val categoryMap = categories.associateBy { it.id }
                val filter = _uiState.value.selectedFilter
                val query = _uiState.value.searchQuery

                // Apply filter
                val filtered = transactions.filter { txn ->
                    val matchesFilter = when (filter) {
                        TransactionFilter.ALL -> true
                        TransactionFilter.EXPENSE -> txn.type == TransactionType.EXPENSE
                        TransactionFilter.INCOME -> txn.type == TransactionType.INCOME
                    }
                    val matchesQuery = query.isEmpty() ||
                            txn.description.contains(query, ignoreCase = true) ||
                            txn.merchantName?.contains(query, ignoreCase = true) == true

                    matchesFilter && matchesQuery
                }

                // Map to TransactionWithCategory and group by date
                val grouped = filtered
                    .map { txn -> TransactionWithCategory(txn, categoryMap[txn.categoryId]) }
                    .groupBy { it.transaction.date }
                    .map { (date, txnsWithCategory) ->
                        val dailyTotal = txnsWithCategory.sumOf { txnWithCategory ->
                            val txn = txnWithCategory.transaction
                            if (txn.type == TransactionType.EXPENSE) -txn.amount else txn.amount
                        }
                        DailyTransactions(
                            date = date,
                            transactionsWithCategory = txnsWithCategory.sortedByDescending { it.transaction.createdAt },
                            dailyTotal = dailyTotal
                        )
                    }
                    .sortedByDescending { it.date }

                grouped
            }
                .catch { e ->
                    _uiState.update { it.copy(isRefreshing = false, error = e.message) }
                }
                .collect { grouped ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            dailyTransactions = grouped
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
