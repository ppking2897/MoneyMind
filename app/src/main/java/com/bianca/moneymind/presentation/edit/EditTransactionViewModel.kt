package com.bianca.moneymind.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.usecase.DeleteTransactionUseCase
import com.bianca.moneymind.domain.usecase.GetCategoriesUseCase
import com.bianca.moneymind.domain.usecase.GetTransactionsUseCase
import com.bianca.moneymind.domain.usecase.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val transactionId: String = savedStateHandle.get<String>("transactionId") ?: ""

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    private var originalTransaction: Transaction? = null

    init {
        loadTransaction()
    }

    private fun loadTransaction() {
        viewModelScope.launch {
            try {
                val transaction = getTransactionsUseCase.byId(transactionId)
                if (transaction == null) {
                    _uiState.update { it.copy(isLoading = false, notFound = true) }
                    return@launch
                }

                originalTransaction = transaction

                // Load categories for the transaction type
                getCategoriesUseCase.byType(transaction.type).collect { categories ->
                    val selectedCategory = categories.find { it.id == transaction.categoryId }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            transactionType = transaction.type,
                            amount = transaction.amount.toString(),
                            description = transaction.description,
                            selectedCategory = selectedCategory,
                            date = transaction.date,
                            note = transaction.note ?: "",
                            categories = categories
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "載入失敗")
                }
            }
        }
    }

    fun onTransactionTypeChange(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type, selectedCategory = null) }
        loadCategoriesForType(type)
    }

    private fun loadCategoriesForType(type: TransactionType) {
        viewModelScope.launch {
            getCategoriesUseCase.byType(type).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun onAmountChange(amount: String) {
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

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, isSaving = true) }

            try {
                deleteTransactionUseCase(transactionId)
                _uiState.update { it.copy(isSaving = false, isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = e.message ?: "刪除失敗")
                }
            }
        }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val original = originalTransaction ?: return

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
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val updatedTransaction = original.copy(
                    type = state.transactionType,
                    amount = amount,
                    description = state.description.trim(),
                    categoryId = state.selectedCategory?.id,
                    date = state.date,
                    note = state.note.takeIf { it.isNotBlank() }
                )

                updateTransactionUseCase(updatedTransaction)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = e.message ?: "儲存失敗")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
