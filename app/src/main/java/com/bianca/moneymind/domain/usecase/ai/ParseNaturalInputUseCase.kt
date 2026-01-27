package com.bianca.moneymind.domain.usecase.ai

import com.bianca.moneymind.domain.model.AiResult
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.ParsedTransactionsResult
import com.bianca.moneymind.domain.repository.AiRepository
import com.bianca.moneymind.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for parsing natural language input into transactions
 *
 * This use case:
 * 1. Gets available categories from repository
 * 2. Sends user input to AI for parsing
 * 3. Returns parsed transaction data
 */
@Singleton
class ParseNaturalInputUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val categoryRepository: CategoryRepository
) {

    /**
     * Parse natural language input
     *
     * @param userInput The user's natural language input (e.g., "今天午餐花了150")
     * @return AiResult containing parsed transactions or error
     */
    suspend operator fun invoke(userInput: String): AiResult<ParsedTransactionsResult> {
        // Get all available categories to help AI categorize
        val categories = categoryRepository.getAll().first()

        // Send to AI for parsing
        return aiRepository.parseNaturalInput(userInput, categories)
    }

    /**
     * Parse with custom category list
     * Useful when categories are already loaded in ViewModel
     */
    suspend fun parseWithCategories(
        userInput: String,
        categories: List<Category>
    ): AiResult<ParsedTransactionsResult> {
        return aiRepository.parseNaturalInput(userInput, categories)
    }
}
