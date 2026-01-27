package com.bianca.moneymind.domain.repository

import com.bianca.moneymind.domain.model.AiResult
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.ParsedTransactionsResult

/**
 * Repository interface for AI operations
 */
interface AiRepository {

    /**
     * Parse natural language input into structured transactions
     */
    suspend fun parseNaturalInput(
        userInput: String,
        categories: List<Category>
    ): AiResult<ParsedTransactionsResult>
}
