package com.bianca.moneymind.domain.usecase

import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> {
        return categoryRepository.getAll()
    }

    fun byType(type: TransactionType): Flow<List<Category>> {
        return categoryRepository.getByType(type)
    }
}
