package com.bianca.moneymind.domain.repository

import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    fun getByType(type: TransactionType): Flow<List<Category>>
    fun getParentCategories(): Flow<List<Category>>
    fun getSubCategories(parentId: String): Flow<List<Category>>
    suspend fun getById(id: String): Category?
    suspend fun add(category: Category)
    suspend fun addAll(categories: List<Category>)
    suspend fun delete(category: Category)
}
