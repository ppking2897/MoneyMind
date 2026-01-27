package com.bianca.moneymind.domain.usecase.ai

import com.bianca.moneymind.domain.model.ParsedTransaction
import com.bianca.moneymind.domain.model.TransactionType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of auto-categorization
 */
data class CategorizationResult(
    val categoryId: String?,
    val suggestedType: TransactionType,
    val confidence: Float,
    val source: MatchSource,
    val needsUserConfirmation: Boolean
)

/**
 * Use case for auto-categorizing transactions
 *
 * Strategy (Offline-First):
 * 1. Try local rules first (keyword/merchant matching) - fast, no network
 * 2. If no match, use AI suggestion from parsed data
 * 3. If still no match, return null and let user choose
 *
 * This reduces API calls and provides instant feedback for common transactions.
 */
@Singleton
class AutoCategorizeUseCase @Inject constructor(
    private val categoryMatcher: CategoryMatcher
) {

    /**
     * Auto-categorize a transaction based on description and optional AI suggestion
     *
     * @param description Transaction description
     * @param merchantName Optional merchant name
     * @param aiSuggestion Optional category suggestion from AI parsing
     * @param aiConfidence Confidence level of AI suggestion (0-1)
     * @return CategorizationResult with category and metadata
     */
    operator fun invoke(
        description: String,
        merchantName: String? = null,
        aiSuggestion: String? = null,
        aiConfidence: Float = 0f
    ): CategorizationResult {
        // Step 1: Try local rule matching first
        val localMatch = categoryMatcher.findMatch(description, merchantName)

        if (localMatch != null && categoryMatcher.shouldUseMatch(localMatch)) {
            return CategorizationResult(
                categoryId = localMatch.categoryId,
                suggestedType = localMatch.suggestedType ?: TransactionType.EXPENSE,
                confidence = localMatch.confidence,
                source = localMatch.matchSource,
                needsUserConfirmation = false
            )
        }

        // Step 2: Use AI suggestion if available
        if (aiSuggestion != null && aiConfidence >= CategoryMatcher.CONFIDENCE_THRESHOLD) {
            return CategorizationResult(
                categoryId = aiSuggestion,
                suggestedType = TransactionType.EXPENSE,
                confidence = aiConfidence,
                source = MatchSource.AI_SUGGESTION,
                needsUserConfirmation = aiConfidence < 0.85f
            )
        }

        // Step 3: Low confidence - need user confirmation
        val bestGuess = localMatch ?: aiSuggestion?.let {
            CategoryMatch(it, aiConfidence, MatchSource.AI_SUGGESTION)
        }

        return CategorizationResult(
            categoryId = bestGuess?.categoryId,
            suggestedType = bestGuess?.suggestedType ?: TransactionType.EXPENSE,
            confidence = bestGuess?.confidence ?: 0f,
            source = bestGuess?.matchSource ?: MatchSource.AI_SUGGESTION,
            needsUserConfirmation = true
        )
    }

    /**
     * Categorize from a parsed transaction
     * Convenient method when using AI parsing results
     */
    fun fromParsedTransaction(parsed: ParsedTransaction): CategorizationResult {
        return invoke(
            description = parsed.description,
            merchantName = parsed.merchantName,
            aiSuggestion = parsed.categoryId,
            aiConfidence = parsed.confidence
        )
    }

    /**
     * Batch categorize multiple transactions
     */
    fun categorizeAll(
        transactions: List<ParsedTransaction>
    ): List<Pair<ParsedTransaction, CategorizationResult>> {
        return transactions.map { parsed ->
            parsed to fromParsedTransaction(parsed)
        }
    }
}
