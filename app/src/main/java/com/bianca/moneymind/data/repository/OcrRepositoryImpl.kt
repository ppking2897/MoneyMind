package com.bianca.moneymind.data.repository

import android.graphics.BitmapFactory
import com.bianca.moneymind.data.remote.gemini.GeminiService
import com.bianca.moneymind.data.remote.gemini.toDomain
import com.bianca.moneymind.domain.model.AiResult
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.ReceiptParseResult
import com.bianca.moneymind.domain.repository.OcrRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of OcrRepository using GeminiService
 */
@Singleton
class OcrRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService
) : OcrRepository {

    override suspend fun parseReceiptImage(
        imageData: ByteArray,
        categories: List<Category>
    ): AiResult<ReceiptParseResult> {
        // Convert byte array to Bitmap (Android-specific, allowed in data layer)
        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            ?: return AiResult.Error(
                com.bianca.moneymind.domain.model.AiException.ParseError("無法解析圖片")
            )

        return try {
            geminiService.retryOnError {
                geminiService.parseReceiptImage(bitmap, categories)
            }.map { it.toDomain() }
        } finally {
            // Recycle bitmap to free memory
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }
}
