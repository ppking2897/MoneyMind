package com.bianca.moneymind.presentation.manual

import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.TransactionType
import java.time.LocalDate

data class ManualInputUiState(
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val description: String = "",
    val selectedCategory: Category? = null,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
