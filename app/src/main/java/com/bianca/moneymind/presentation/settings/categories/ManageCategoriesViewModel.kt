package com.bianca.moneymind.presentation.settings.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.usecase.GetCategoriesUseCase
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
class ManageCategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageCategoriesUiState())
    val uiState: StateFlow<ManageCategoriesUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadCategories()
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(selectedType = type, isLoading = true) }
        loadCategories()
    }

    private fun loadCategories() {
        loadJob?.cancel()

        val type = _uiState.value.selectedType

        loadJob = viewModelScope.launch {
            getCategoriesUseCase.byType(type)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { categories ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            categories = categories.sortedBy { cat -> cat.sortOrder }
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
