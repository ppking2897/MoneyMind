package com.bianca.moneymind.presentation.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.InputType
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.usecase.AddTransactionUseCase
import com.bianca.moneymind.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ManualInputViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualInputUiState())
    val uiState: StateFlow<ManualInputUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase.byType(_uiState.value.transactionType).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun onTransactionTypeChange(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type, selectedCategory = null) }
        loadCategories()
    }

    fun onAmountChange(amount: String) {
        // Only allow valid decimal input
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(amount = amount) }
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onCategorySelect(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveTransaction() {
        val state = _uiState.value

        // Validation
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "請輸入有效金額") }
            return
        }

        if (state.description.isBlank()) {
            _uiState.update { it.copy(error = "請輸入描述") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    type = state.transactionType,
                    amount = amount,
                    description = state.description.trim(),
                    categoryId = state.selectedCategory?.id,
                    date = state.date,
                    createdAt = Instant.now(),
                    inputType = InputType.MANUAL,
                    receiptImagePath = null,
                    merchantName = null,
                    note = state.note.takeIf { it.isNotBlank() },
                    rawInput = null
                )

                addTransactionUseCase(transaction)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "儲存失敗") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
