package com.bianca.moneymind.data.remote.gemini

import com.bianca.moneymind.domain.model.ParsedReceipt
import com.bianca.moneymind.domain.model.ParsedTransaction
import com.bianca.moneymind.domain.model.ParsedTransactionsResult
import com.bianca.moneymind.domain.model.ReceiptItem
import com.bianca.moneymind.domain.model.ReceiptParseResult

/**
 * Mapper functions for converting between Data DTOs and Domain Models
 */

fun ParsedTransactionDto.toDomain(): ParsedTransaction = ParsedTransaction(
    type = type,
    amount = amount,
    date = date,
    merchantName = merchantName,
    categoryId = categoryId,
    description = description,
    missingFields = missingFields,
    confidence = confidence
)

fun ParsedTransactionsResponse.toDomain(): ParsedTransactionsResult = ParsedTransactionsResult(
    transactions = transactions.map { it.toDomain() }
)

fun ReceiptItemDto.toDomain(): ReceiptItem = ReceiptItem(
    name = name,
    quantity = quantity,
    unitPrice = unitPrice,
    totalPrice = totalPrice
)

fun ReceiptParseDto.toDomain(): ParsedReceipt = ParsedReceipt(
    merchantName = merchantName,
    totalAmount = totalAmount,
    date = date,
    items = items.map { it.toDomain() },
    paymentMethod = paymentMethod,
    confidence = confidence
)

fun ReceiptParseResponse.toDomain(): ReceiptParseResult = ReceiptParseResult(
    receipt = receipt.toDomain(),
    suggestedCategoryId = suggestedCategoryId,
    suggestedType = suggestedType
)
