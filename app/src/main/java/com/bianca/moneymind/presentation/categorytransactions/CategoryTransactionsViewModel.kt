package com.bianca.moneymind.presentation.categorytransactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.usecase.GetCategoriesUseCase
import com.bianca.moneymind.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryTransactionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val categoryId: String = savedStateHandle.get<String>("categoryId") ?: ""

    private val _uiState = MutableStateFlow(CategoryTransactionsUiState(categoryId = categoryId))
    val uiState: StateFlow<CategoryTransactionsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadCategoryAndTransactions()
    }

    private fun loadCategoryAndTransactions() {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            // Load category name
            val category = getCategoriesUseCase.byId(categoryId)
            val categoryName = category?.name ?: "未知類別"

            _uiState.update { it.copy(categoryName = categoryName) }

            // Load transactions for this category
            getTransactionsUseCase.byCategory(categoryId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { transactions ->
                    val groupedTransactions = transactions
                        .groupBy { it.date }
                        .toSortedMap(reverseOrder())

                    val totalAmount = transactions.sumOf { it.amount }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            transactions = groupedTransactions,
                            totalAmount = totalAmount,
                            transactionCount = transactions.size
                        )
                    }
                }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadCategoryAndTransactions()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
