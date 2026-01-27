package com.bianca.moneymind.data.local.mapper

import com.bianca.moneymind.data.local.entity.TransactionEntity
import com.bianca.moneymind.domain.model.InputType
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Convert epoch day value to LocalDate with fallback for milliseconds
 * Some legacy data may have been stored as milliseconds instead of epoch days
 */
private fun parseDate(value: Long): LocalDate {
    // Epoch days for year 3000 would be around 376000
    // If value is larger, it's likely milliseconds
    return if (value > 1_000_000) {
        // Value is probably milliseconds, convert to LocalDate
        Instant.ofEpochMilli(value)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } else {
        LocalDate.ofEpochDay(value)
    }
}

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    type = TransactionType.valueOf(type),
    amount = amount,
    description = description,
    categoryId = categoryId,
    date = parseDate(date),
    createdAt = Instant.ofEpochMilli(createdAt),
    inputType = InputType.valueOf(inputType),
    receiptImagePath = receiptImagePath,
    merchantName = merchantName,
    note = note,
    rawInput = rawInput
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    type = type.name,
    amount = amount,
    description = description,
    categoryId = categoryId,
    date = date.toEpochDay(),
    createdAt = createdAt.toEpochMilli(),
    inputType = inputType.name,
    receiptImagePath = receiptImagePath,
    merchantName = merchantName,
    note = note,
    rawInput = rawInput
)

fun List<TransactionEntity>.toDomainList(): List<Transaction> = map { it.toDomain() }
