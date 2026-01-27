package com.bianca.moneymind.domain.model

import java.util.UUID

object DefaultCategories {

    val expenseCategories = listOf(
        Category(
            id = "expense_food",
            name = "餐飲",
            icon = "restaurant",
            color = "#FF5722",
            type = TransactionType.EXPENSE,
            parentId = null,
            isDefault = true,
            sortOrder = 1
        ),
        Category(
            id = "expense_transport",
            name = "交通",
            icon = "directions_car",
            color = "#2196F3",
            type = TransactionType.EXPENSE,
            parentId = null,
            isDefault = true,
            sortOrder = 2
        ),
        Category(
            id = "expense_shopping",
            name = "購物",
            icon = "shopping_bag",
            color = "#E91E63",
            type = TransactionType.EXPENSE,
            parentId = null,
            isDefault = true,
            sortOrder = 3
        ),
        Category(
            id = "expense_housing",
            name = "居住",
            icon = "home",
            color = "#795548",
            type = TransactionType.EXPENSE,
            parentId = null,
            isDefault = true,
            sortOrder = 4
        ),
        Category(
            id = "expense_entertainment",
            name = "娛樂",
            icon = "sports_esports",
            color = "#9C27B0",
            type = TransactionType.EXPENSE,
            parentId = null,
            isDefault = true,
            sortOrder = 5
        ),
        Category(
            id = "expense_medical",
            name = "醫療",
            icon = "local_hospital",
            color = "#F44336",
            type = TransactionType.EXPENSE,
            parentId = null,
            isDefault = true,
            sortOrder = 6
        ),
        Category(
            id = "expense_education",
            name = "教育",
            icon = "school",
            color = "#3F51B5",
            type = TransactionType.EXPENSE,
            parentId = null,
            isDefault = true,
            sortOrder = 7
        ),
        Category(
            id = "expense_other",
            name = "其他",
            icon = "more_horiz",
            color = "#607D8B",
            type = TransactionType.EXPENSE,
            parentId = null,
            isDefault = true,
            sortOrder = 99
        )
    )

    val incomeCategories = listOf(
        Category(
            id = "income_salary",
            name = "薪水",
            icon = "work",
            color = "#4CAF50",
            type = TransactionType.INCOME,
            parentId = null,
            isDefault = true,
            sortOrder = 1
        ),
        Category(
            id = "income_investment",
            name = "投資",
            icon = "trending_up",
            color = "#00BCD4",
            type = TransactionType.INCOME,
            parentId = null,
            isDefault = true,
            sortOrder = 2
        ),
        Category(
            id = "income_bonus",
            name = "獎金",
            icon = "card_giftcard",
            color = "#FFC107",
            type = TransactionType.INCOME,
            parentId = null,
            isDefault = true,
            sortOrder = 3
        ),
        Category(
            id = "income_other",
            name = "其他收入",
            icon = "attach_money",
            color = "#8BC34A",
            type = TransactionType.INCOME,
            parentId = null,
            isDefault = true,
            sortOrder = 99
        )
    )

    val all: List<Category> = expenseCategories + incomeCategories
}
