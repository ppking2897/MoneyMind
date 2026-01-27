package com.bianca.moneymind.domain.repository

import com.bianca.moneymind.domain.model.AiResult
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.ReceiptParseResult

/**
 * Repository interface for OCR/Receipt parsing operations
 */
interface OcrRepository {

    /**
     * Parse a receipt image into structured data
     *
     * @param imageData The receipt image as byte array
     * @param categories Available categories for suggestion
     * @return Parsed receipt result
     */
    suspend fun parseReceiptImage(
        imageData: ByteArray,
        categories: List<Category>
    ): AiResult<ReceiptParseResult>
}
