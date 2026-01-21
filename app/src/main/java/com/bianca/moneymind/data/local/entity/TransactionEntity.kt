package com.bianca.moneymind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("date"),
        Index("type")
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val amount: Double,
    val description: String,
    val categoryId: String?,
    val date: Long,
    val createdAt: Long,
    val inputType: String,
    val receiptImagePath: String?,
    val merchantName: String?,
    val note: String?,
    val rawInput: String?
)
