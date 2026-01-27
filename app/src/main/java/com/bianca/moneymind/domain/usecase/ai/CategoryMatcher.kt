package com.bianca.moneymind.domain.usecase.ai

import com.bianca.moneymind.domain.model.TransactionType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of category matching
 */
data class CategoryMatch(
    val categoryId: String,
    val confidence: Float,
    val matchSource: MatchSource,
    val suggestedType: TransactionType? = null
)

/**
 * Source of the category match
 */
enum class MatchSource {
    USER_RULE,      // User's learned preference (highest priority)
    KEYWORD_RULE,   // Keyword matching
    MERCHANT_RULE,  // Merchant name matching
    AI_SUGGESTION   // From Gemini API
}

/**
 * Matches transaction descriptions to categories using rules
 */
@Singleton
class CategoryMatcher @Inject constructor() {

    /**
     * Find the best category match for a transaction
     *
     * Priority:
     * 1. User rules (future: from UserCategoryRule table)
     * 2. Keyword rules
     * 3. Merchant rules
     *
     * @param description Transaction description
     * @param merchantName Optional merchant name
     * @return CategoryMatch if found, null otherwise
     */
    fun findMatch(
        description: String,
        merchantName: String? = null
    ): CategoryMatch? {
        val normalizedDescription = description.lowercase().trim()
        val normalizedMerchant = merchantName?.lowercase()?.trim()

        // TODO: Check user rules first (Phase 3 - learning mechanism)
        // val userMatch = findUserRuleMatch(normalizedDescription)
        // if (userMatch != null) return userMatch

        // Check keyword rules
        val keywordMatch = findKeywordMatch(normalizedDescription)
        if (keywordMatch != null) return keywordMatch

        // Check merchant rules
        if (normalizedMerchant != null) {
            val merchantMatch = findMerchantMatch(normalizedMerchant)
            if (merchantMatch != null) return merchantMatch
        }

        // Also check if description contains merchant name
        val descriptionMerchantMatch = findMerchantMatchInDescription(normalizedDescription)
        if (descriptionMerchantMatch != null) return descriptionMerchantMatch

        return null
    }

    /**
     * Find match using keyword rules
     */
    private fun findKeywordMatch(description: String): CategoryMatch? {
        for (rule in DefaultRules.keywordRules) {
            for (keyword in rule.keywords) {
                if (description.contains(keyword.lowercase())) {
                    return CategoryMatch(
                        categoryId = rule.categoryId,
                        confidence = 0.9f,
                        matchSource = MatchSource.KEYWORD_RULE,
                        suggestedType = rule.type
                    )
                }
            }
        }
        return null
    }

    /**
     * Find match using merchant rules
     */
    private fun findMerchantMatch(merchantName: String): CategoryMatch? {
        for (rule in DefaultRules.merchantRules) {
            if (merchantName.contains(rule.merchantName.lowercase()) ||
                rule.merchantName.lowercase().contains(merchantName)) {
                return CategoryMatch(
                    categoryId = rule.categoryId,
                    confidence = 0.9f,
                    matchSource = MatchSource.MERCHANT_RULE,
                    suggestedType = rule.type
                )
            }
        }
        return null
    }

    /**
     * Find merchant match within the description text
     */
    private fun findMerchantMatchInDescription(description: String): CategoryMatch? {
        for (rule in DefaultRules.merchantRules) {
            if (description.contains(rule.merchantName.lowercase())) {
                return CategoryMatch(
                    categoryId = rule.categoryId,
                    confidence = 0.85f,
                    matchSource = MatchSource.MERCHANT_RULE,
                    suggestedType = rule.type
                )
            }
        }
        return null
    }

    /**
     * Check if we should use the category match or ask the user
     */
    fun shouldUseMatch(match: CategoryMatch): Boolean {
        return match.confidence >= CONFIDENCE_THRESHOLD
    }

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.7f
    }
}
