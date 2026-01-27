package com.bianca.moneymind.presentation.settings.categories

import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.TransactionType

data class ManageCategoriesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val categories: List<Category> = emptyList()
)
