package com.bianca.moneymind.data.local.mapper

import com.bianca.moneymind.data.local.entity.TransactionEntity
import com.bianca.moneymind.domain.model.InputType
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    type = TransactionType.valueOf(type),
    amount = amount,
    description = description,
    categoryId = categoryId,
    date = LocalDate.ofEpochDay(date),
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
