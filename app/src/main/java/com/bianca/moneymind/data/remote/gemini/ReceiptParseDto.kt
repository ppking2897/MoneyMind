package com.bianca.moneymind.data.remote.gemini

import com.bianca.moneymind.domain.model.TransactionType
import java.time.LocalDate

/**
 * Parsed receipt data from Gemini
 */
data class ReceiptParseDto(
    val merchantName: String?,
    val totalAmount: Double?,
    val date: LocalDate?,
    val items: List<ReceiptItemDto>,
    val paymentMethod: String?,
    val confidence: Float
)

/**
 * Individual item on a receipt
 */
data class ReceiptItemDto(
    val name: String,
    val quantity: Int,
    val unitPrice: Double?,
    val totalPrice: Double?
)

/**
 * Response from receipt parsing
 */
data class ReceiptParseResponse(
    val receipt: ReceiptParseDto,
    val suggestedCategoryId: String?,
    val suggestedType: TransactionType = TransactionType.EXPENSE
)
