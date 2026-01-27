package com.bianca.moneymind.domain.model

import java.time.LocalDate

/**
 * Domain model for parsed receipt data
 */
data class ParsedReceipt(
    val merchantName: String?,
    val totalAmount: Double?,
    val date: LocalDate?,
    val items: List<ReceiptItem>,
    val paymentMethod: String?,
    val confidence: Float
)

/**
 * Individual item on a receipt
 */
data class ReceiptItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double?,
    val totalPrice: Double?
)

/**
 * Result from receipt parsing
 */
data class ReceiptParseResult(
    val receipt: ParsedReceipt,
    val suggestedCategoryId: String?,
    val suggestedType: TransactionType = TransactionType.EXPENSE
)
