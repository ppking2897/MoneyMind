package com.bianca.moneymind.domain.usecase.ocr

import com.bianca.moneymind.domain.model.AiResult
import com.bianca.moneymind.domain.model.ReceiptParseResult
import com.bianca.moneymind.domain.repository.CategoryRepository
import com.bianca.moneymind.domain.repository.OcrRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for scanning and parsing receipts
 *
 * Uses OcrRepository to parse receipt images
 */
@Singleton
class ParseReceiptUseCase @Inject constructor(
    private val ocrRepository: OcrRepository,
    private val categoryRepository: CategoryRepository
) {

    /**
     * Parse a receipt image
     *
     * @param imageData The receipt image as byte array
     * @return AiResult containing parse results
     */
    suspend operator fun invoke(imageData: ByteArray): AiResult<ReceiptParseResult> {
        val categories = categoryRepository.getAll().first()
        return ocrRepository.parseReceiptImage(imageData, categories)
    }
}
