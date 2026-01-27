package com.bianca.moneymind.presentation.edit

import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.TransactionType
import java.time.LocalDate

data class EditTransactionUiState(
    val isLoading: Boolean = true,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val description: String = "",
    val selectedCategory: Category? = null,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val error: String? = null,
    val notFound: Boolean = false
) {
    companion object {
        private val mockCategories = listOf(
            Category("expense_food", "餐飲", "restaurant", "#FF5722", TransactionType.EXPENSE, null, true, 1),
            Category("expense_transport", "交通", "directions_car", "#2196F3", TransactionType.EXPENSE, null, true, 2),
            Category("expense_shopping", "購物", "shopping_bag", "#E91E63", TransactionType.EXPENSE, null, true, 3),
            Category("expense_entertainment", "娛樂", "sports_esports", "#9C27B0", TransactionType.EXPENSE, null, true, 4)
        )

        fun mock() = EditTransactionUiState(
            isLoading = false,
            transactionType = TransactionType.EXPENSE,
            amount = "85",
            description = "午餐便當",
            selectedCategory = mockCategories.first(),
            date = LocalDate.now(),
            note = "",
            categories = mockCategories
        )

        fun mockLoading() = EditTransactionUiState(
            isLoading = true
        )
    }
}
