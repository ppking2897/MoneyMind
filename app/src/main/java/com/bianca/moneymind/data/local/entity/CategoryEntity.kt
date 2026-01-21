package com.bianca.moneymind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: String,
    val parentId: String?,
    val isDefault: Boolean,
    val sortOrder: Int
)
