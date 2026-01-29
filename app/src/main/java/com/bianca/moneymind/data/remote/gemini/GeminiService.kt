package com.bianca.moneymind.data.remote.gemini

import android.graphics.Bitmap
import com.bianca.moneymind.domain.model.AiException
import com.bianca.moneymind.domain.model.AiResult
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.TransactionType
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for interacting with Gemini API
 */
@Singleton
class GeminiService @Inject constructor(
    private val promptBuilder: PromptBuilder
) {
    private var generativeModel: GenerativeModel? = null

    /**
     * Initialize the Gemini model with API key
     */
    fun initialize(apiKey: String) {
        generativeModel = GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.3f
                topK = 32
                topP = 0.95f
                maxOutputTokens = 4096
            }
        )
    }

    /**
     * Parse natural language input into transactions
     */
    suspend fun parseNaturalInput(
        userInput: String,
        categories: List<Category>
    ): AiResult<ParsedTransactionsResponse> = withContext(Dispatchers.IO) {
        val model = generativeModel
            ?: return@withContext AiResult.Error(AiException.ApiKeyMissing)

        try {
            val prompt = promptBuilder.buildParsePrompt(userInput, categories)
            val response = model.generateContent(prompt)

            parseTransactionsResponse(response)
        } catch (e: IOException) {
            AiResult.Error(AiException.NetworkError)
        } catch (e: Exception) {
            when {
                e.message?.contains("rate limit", ignoreCase = true) == true ->
                    AiResult.Error(AiException.RateLimitExceeded)
                else ->
                    AiResult.Error(AiException.Unknown(e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Parse the Gemini response into ParsedTransactionsResponse
     */
    private fun parseTransactionsResponse(
        response: GenerateContentResponse
    ): AiResult<ParsedTransactionsResponse> {
        val text = response.text?.trim()
            ?: return AiResult.Error(AiException.InvalidResponse)

        // Remove markdown code block if present
        val jsonText = text
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val jsonObject = JSONObject(jsonText)
            val transactionsArray = jsonObject.getJSONArray("transactions")

            val transactions = (0 until transactionsArray.length()).map { i ->
                parseTransactionFromJson(transactionsArray.getJSONObject(i))
            }

            AiResult.Success(ParsedTransactionsResponse(transactions))
        } catch (e: Exception) {
            AiResult.Error(AiException.ParseError(e.message ?: "JSON parse error"))
        }
    }

    /**
     * Parse a single transaction from JSON
     */
    private fun parseTransactionFromJson(json: JSONObject): ParsedTransactionDto {
        val typeStr = json.optString("type", "EXPENSE")
        val type = if (typeStr == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE

        val amount = if (json.isNull("amount")) null else json.optDouble("amount")

        val dateStr = json.optString("date", LocalDate.now().toString())
        val date = try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            LocalDate.now()
        }

        val missingFields = mutableListOf<String>()
        val missingArray = json.optJSONArray("missingFields")
        if (missingArray != null) {
            for (i in 0 until missingArray.length()) {
                missingFields.add(missingArray.getString(i))
            }
        }

        return ParsedTransactionDto(
            type = type,
            amount = amount,
            date = date,
            merchantName = json.optStringOrNull("merchantName"),
            categoryId = json.optStringOrNull("categoryId"),
            description = json.optString("description", ""),
            missingFields = missingFields,
            confidence = json.optDouble("confidence", 0.8).toFloat()
        )
    }

    private fun JSONObject.optStringOrNull(key: String): String? {
        return if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
    }

    /**
     * Parse receipt image directly using Gemini Vision
     * This is more accurate than OCR + text parsing
     */
    suspend fun parseReceiptImage(
        bitmap: Bitmap,
        categories: List<Category>
    ): AiResult<ReceiptParseResponse> = withContext(Dispatchers.IO) {
        val model = generativeModel
            ?: return@withContext AiResult.Error(AiException.ApiKeyMissing)

        try {
            val prompt = promptBuilder.buildReceiptImagePrompt(categories)

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = model.generateContent(inputContent)
            parseReceiptResponse(response)
        } catch (e: IOException) {
            AiResult.Error(AiException.NetworkError)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            when {
                errorMsg.contains("rate limit", ignoreCase = true) ->
                    AiResult.Error(AiException.RateLimitExceeded)
                errorMsg.contains("quota", ignoreCase = true) ->
                    AiResult.Error(AiException.RateLimitExceeded)
                errorMsg.contains("token", ignoreCase = true) ||
                errorMsg.contains("length", ignoreCase = true) ||
                errorMsg.contains("too long", ignoreCase = true) ->
                    AiResult.Error(AiException.ParseError("圖片過大: $errorMsg"))
                else ->
                    AiResult.Error(AiException.Unknown(errorMsg))
            }
        }
    }

    /**
     * Parse receipt OCR text into structured data (fallback method)
     */
    @Deprecated("Use parseReceiptImage for better accuracy")
    suspend fun parseReceipt(
        ocrText: String,
        categories: List<Category>
    ): AiResult<ReceiptParseResponse> = withContext(Dispatchers.IO) {
        val model = generativeModel
            ?: return@withContext AiResult.Error(AiException.ApiKeyMissing)

        try {
            val prompt = promptBuilder.buildReceiptParsePrompt(ocrText, categories)
            val response = model.generateContent(prompt)

            parseReceiptResponse(response)
        } catch (e: IOException) {
            AiResult.Error(AiException.NetworkError)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            when {
                errorMsg.contains("rate limit", ignoreCase = true) ->
                    AiResult.Error(AiException.RateLimitExceeded)
                errorMsg.contains("quota", ignoreCase = true) ->
                    AiResult.Error(AiException.RateLimitExceeded)
                errorMsg.contains("token", ignoreCase = true) ||
                errorMsg.contains("length", ignoreCase = true) ||
                errorMsg.contains("too long", ignoreCase = true) ->
                    AiResult.Error(AiException.ParseError("輸入過長: $errorMsg"))
                else ->
                    AiResult.Error(AiException.Unknown(errorMsg))
            }
        }
    }

    /**
     * Parse the Gemini response for receipt (simplified format)
     */
    private fun parseReceiptResponse(
        response: GenerateContentResponse
    ): AiResult<ReceiptParseResponse> {
        val text = response.text?.trim()
            ?: return AiResult.Error(AiException.InvalidResponse)

        val jsonText = text
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val json = JSONObject(jsonText)

            val dateStr = json.optStringOrNull("date")
            val date = dateStr?.let {
                try {
                    LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    null
                }
            }

            val receipt = ReceiptParseDto(
                merchantName = json.optStringOrNull("merchantName"),
                totalAmount = if (json.isNull("totalAmount")) null else json.optDouble("totalAmount"),
                date = date,
                items = emptyList(), // Simplified - no items parsing
                paymentMethod = null,
                confidence = json.optDouble("confidence", 0.5).toFloat()
            )

            AiResult.Success(
                ReceiptParseResponse(
                    receipt = receipt,
                    suggestedCategoryId = json.optStringOrNull("suggestedCategoryId"),
                    suggestedType = TransactionType.EXPENSE
                )
            )
        } catch (e: Exception) {
            AiResult.Error(AiException.ParseError(e.message ?: "JSON parse error"))
        }
    }

    /**
     * Retry wrapper for network errors
     */
    suspend fun <T> retryOnError(
        times: Int = 2,
        block: suspend () -> AiResult<T>
    ): AiResult<T> {
        repeat(times) {
            when (val result = block()) {
                is AiResult.Success -> return result
                is AiResult.Error -> {
                    if (result.exception == AiException.NetworkError) {
                        kotlinx.coroutines.delay(1000)
                    } else {
                        return result
                    }
                }
            }
        }
        return block()
    }
}
