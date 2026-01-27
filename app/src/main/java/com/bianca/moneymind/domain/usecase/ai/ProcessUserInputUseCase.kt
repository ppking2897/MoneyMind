package com.bianca.moneymind.domain.usecase.ai

import com.bianca.moneymind.domain.model.AiResult
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.ParsedTransaction
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processed transaction ready for user review
 */
data class ProcessedTransaction(
    val parsed: ParsedTransaction,
    val categorization: CategorizationResult,
    val displayDescription: String,
    val isComplete: Boolean
) {
    /**
     * Check if this transaction is ready to save without user input
     */
    val canAutoSave: Boolean
        get() = isComplete && !categorization.needsUserConfirmation && parsed.amount != null
}

/**
 * Result of processing user input
 */
data class ProcessResult(
    val transactions: List<ProcessedTransaction>,
    val hasIncompleteTransactions: Boolean,
    val followUpQuestion: String?
)

/**
 * Combined use case for processing user input end-to-end
 *
 * This orchestrates:
 * 1. AI parsing of natural language
 * 2. Auto-categorization of each transaction
 * 3. Determining if follow-up is needed
 */
@Singleton
class ProcessUserInputUseCase @Inject constructor(
    private val parseUseCase: ParseNaturalInputUseCase,
    private val categorizeUseCase: AutoCategorizeUseCase
) {

    /**
     * Process user's natural language input
     *
     * @param userInput The user's message (e.g., "今天午餐150，晚餐200")
     * @return AiResult containing processed transactions
     */
    suspend operator fun invoke(userInput: String): AiResult<ProcessResult> {
        // Step 1: Parse with AI
        val parseResult = parseUseCase(userInput)

        return parseResult.map { response ->
            // Step 2: Categorize each parsed transaction
            val processed = response.transactions.map { parsed ->
                val categorization = categorizeUseCase.fromParsedTransaction(parsed)

                ProcessedTransaction(
                    parsed = parsed,
                    categorization = categorization,
                    displayDescription = buildDisplayDescription(parsed),
                    isComplete = parsed.missingFields.isEmpty()
                )
            }

            // Step 3: Determine if follow-up is needed
            val incomplete = processed.filter { !it.isComplete }
            val followUp = if (incomplete.isNotEmpty()) {
                buildFollowUpQuestion(incomplete)
            } else {
                null
            }

            ProcessResult(
                transactions = processed,
                hasIncompleteTransactions = incomplete.isNotEmpty(),
                followUpQuestion = followUp
            )
        }
    }

    /**
     * Process with pre-loaded categories
     */
    suspend fun processWithCategories(
        userInput: String,
        categories: List<Category>
    ): AiResult<ProcessResult> {
        val parseResult = parseUseCase.parseWithCategories(userInput, categories)

        return parseResult.map { response ->
            val processed = response.transactions.map { parsed ->
                val categorization = categorizeUseCase.fromParsedTransaction(parsed)

                ProcessedTransaction(
                    parsed = parsed,
                    categorization = categorization,
                    displayDescription = buildDisplayDescription(parsed),
                    isComplete = parsed.missingFields.isEmpty()
                )
            }

            val incomplete = processed.filter { !it.isComplete }
            val followUp = if (incomplete.isNotEmpty()) {
                buildFollowUpQuestion(incomplete)
            } else {
                null
            }

            ProcessResult(
                transactions = processed,
                hasIncompleteTransactions = incomplete.isNotEmpty(),
                followUpQuestion = followUp
            )
        }
    }

    /**
     * Build a human-readable description for display
     */
    private fun buildDisplayDescription(parsed: ParsedTransaction): String {
        val parts = mutableListOf<String>()

        parsed.merchantName?.let { parts.add(it) }

        if (parts.isEmpty()) {
            parts.add(parsed.description)
        }

        return parts.joinToString(" - ")
    }

    /**
     * Build follow-up question for incomplete transactions
     */
    private fun buildFollowUpQuestion(incomplete: List<ProcessedTransaction>): String {
        val allMissing = incomplete.flatMap { it.parsed.missingFields }.distinct()

        return when {
            "amount" in allMissing && allMissing.size == 1 -> {
                "請問金額是多少？"
            }
            "categoryId" in allMissing && allMissing.size == 1 -> {
                "請問這筆消費的類別是什麼？"
            }
            allMissing.size > 1 -> {
                val missingText = allMissing.joinToString("、") { field ->
                    when (field) {
                        "amount" -> "金額"
                        "categoryId" -> "類別"
                        "date" -> "日期"
                        else -> field
                    }
                }
                "請補充以下資訊：$missingText"
            }
            else -> "還需要什麼資訊嗎？"
        }
    }
}
