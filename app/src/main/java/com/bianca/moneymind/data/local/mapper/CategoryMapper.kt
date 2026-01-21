package com.bianca.moneymind.data.local.mapper

import com.bianca.moneymind.data.local.entity.CategoryEntity
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.TransactionType

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = TransactionType.valueOf(type),
    parentId = parentId,
    isDefault = isDefault,
    sortOrder = sortOrder
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type.name,
    parentId = parentId,
    isDefault = isDefault,
    sortOrder = sortOrder
)

fun List<CategoryEntity>.toDomainList(): List<Category> = map { it.toDomain() }
