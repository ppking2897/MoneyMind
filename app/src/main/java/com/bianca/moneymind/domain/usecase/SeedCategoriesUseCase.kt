package com.bianca.moneymind.domain.usecase

import com.bianca.moneymind.domain.model.DefaultCategories
import com.bianca.moneymind.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SeedCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke() {
        val existingCategories = categoryRepository.getAll().first()
        if (existingCategories.isEmpty()) {
            categoryRepository.addAll(DefaultCategories.all)
        }
    }
}
