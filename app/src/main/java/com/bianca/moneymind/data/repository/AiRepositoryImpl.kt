package com.bianca.moneymind.data.repository

import com.bianca.moneymind.data.remote.gemini.GeminiService
import com.bianca.moneymind.data.remote.gemini.toDomain
import com.bianca.moneymind.domain.model.AiResult
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.ParsedTransactionsResult
import com.bianca.moneymind.domain.repository.AiRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AiRepository using GeminiService
 */
@Singleton
class AiRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService
) : AiRepository {

    override suspend fun parseNaturalInput(
        userInput: String,
        categories: List<Category>
    ): AiResult<ParsedTransactionsResult> {
        return geminiService.retryOnError {
            geminiService.parseNaturalInput(userInput, categories)
        }.map { it.toDomain() }
    }
}
