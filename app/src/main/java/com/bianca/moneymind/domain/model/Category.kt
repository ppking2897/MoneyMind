package com.bianca.moneymind.domain.model

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: TransactionType,
    val parentId: String?,
    val isDefault: Boolean,
    val sortOrder: Int
)
