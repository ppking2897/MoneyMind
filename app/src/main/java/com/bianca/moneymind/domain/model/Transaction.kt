package com.bianca.moneymind.domain.model

import java.time.Instant
import java.time.LocalDate

data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val categoryId: String?,
    val date: LocalDate,
    val createdAt: Instant,
    val inputType: InputType,
    val receiptImagePath: String?,
    val merchantName: String?,
    val note: String?,
    val rawInput: String?
)
