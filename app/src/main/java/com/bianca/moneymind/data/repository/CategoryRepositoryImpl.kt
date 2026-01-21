package com.bianca.moneymind.data.repository

import com.bianca.moneymind.data.local.dao.CategoryDao
import com.bianca.moneymind.data.local.mapper.toDomain
import com.bianca.moneymind.data.local.mapper.toDomainList
import com.bianca.moneymind.data.local.mapper.toEntity
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> {
        return categoryDao.getAll().map { it.toDomainList() }
    }

    override fun getByType(type: TransactionType): Flow<List<Category>> {
        return categoryDao.getByType(type.name).map { it.toDomainList() }
    }

    override fun getParentCategories(): Flow<List<Category>> {
        return categoryDao.getParentCategories().map { it.toDomainList() }
    }

    override fun getSubCategories(parentId: String): Flow<List<Category>> {
        return categoryDao.getSubCategories(parentId).map { it.toDomainList() }
    }

    override suspend fun getById(id: String): Category? {
        return categoryDao.getById(id)?.toDomain()
    }

    override suspend fun add(category: Category) {
        categoryDao.insert(category.toEntity())
    }

    override suspend fun addAll(categories: List<Category>) {
        categoryDao.insertAll(categories.map { it.toEntity() })
    }

    override suspend fun delete(category: Category) {
        categoryDao.delete(category.toEntity())
    }
}
