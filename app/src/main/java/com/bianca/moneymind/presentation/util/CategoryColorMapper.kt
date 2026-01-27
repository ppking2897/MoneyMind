package com.bianca.moneymind.presentation.util

import androidx.compose.ui.graphics.Color

/**
 * Utility object for mapping category names to colors.
 * Provides consistent color scheme across the app.
 */
object CategoryColorMapper {

    // Standard colors for transaction types
    val ExpenseColor = Color(0xFFE53935)  // Red
    val IncomeColor = Color(0xFF43A047)   // Green
    val WarningColor = Color(0xFFFFA000)  // Orange/Amber

    // Default category colors mapped by category name/icon
    private val categoryColorMap = mapOf(
        // Expense categories
        "restaurant" to Color(0xFFFF5722),     // Deep Orange - Food
        "directions_car" to Color(0xFF2196F3), // Blue - Transportation
        "shopping_bag" to Color(0xFFE91E63),   // Pink - Shopping
        "home" to Color(0xFF795548),           // Brown - Housing
        "sports_esports" to Color(0xFF9C27B0), // Purple - Entertainment
        "local_hospital" to Color(0xFFF44336), // Red - Medical
        "school" to Color(0xFF3F51B5),         // Indigo - Education
        "more_horiz" to Color(0xFF607D8B),     // Blue Grey - Other

        // Income categories
        "work" to Color(0xFF4CAF50),           // Green - Salary
        "trending_up" to Color(0xFF00BCD4),    // Cyan - Investment
        "card_giftcard" to Color(0xFFFF9800),  // Orange - Bonus
        "attach_money" to Color(0xFF8BC34A),   // Light Green - Other Income

        // Chinese category names mapping
        "food" to Color(0xFFFF5722),
        "transportation" to Color(0xFF2196F3),
        "shopping" to Color(0xFFE91E63),
        "housing" to Color(0xFF795548),
        "entertainment" to Color(0xFF9C27B0),
        "medical" to Color(0xFFF44336),
        "education" to Color(0xFF3F51B5),
        "other" to Color(0xFF607D8B),
        "salary" to Color(0xFF4CAF50),
        "investment" to Color(0xFF00BCD4),
        "bonus" to Color(0xFFFF9800)
    )

    // Pie chart color palette for dynamic assignment
    val pieChartColors = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF607D8B), // Blue Grey
        Color(0xFF795548), // Brown
        Color(0xFF8BC34A)  // Light Green
    )

    /**
     * Gets the color for a category based on its icon name.
     *
     * @param iconName The icon name (e.g., "restaurant", "directions_car")
     * @return The corresponding color, or default gray if not found
     */
    fun getCategoryColor(iconName: String?): Color {
        if (iconName == null) return Color.Gray
        return categoryColorMap[iconName.lowercase()] ?: Color.Gray
    }

    /**
     * Gets the color for a category based on its name (Chinese or English).
     *
     * @param categoryName The category name
     * @return The corresponding color, or default gray if not found
     */
    fun getCategoryColorByName(categoryName: String?): Color {
        if (categoryName == null) return Color.Gray

        // Try direct match first
        categoryColorMap[categoryName.lowercase()]?.let { return it }

        // Try Chinese name mapping
        return when (categoryName) {
            "food", "Food", "food" -> categoryColorMap["restaurant"]
            "transportation", "Transportation" -> categoryColorMap["directions_car"]
            "shopping", "Shopping" -> categoryColorMap["shopping_bag"]
            "housing", "Housing" -> categoryColorMap["home"]
            "entertainment", "Entertainment" -> categoryColorMap["sports_esports"]
            "medical", "Medical" -> categoryColorMap["local_hospital"]
            "education", "Education" -> categoryColorMap["school"]
            "salary", "Salary" -> categoryColorMap["work"]
            "investment", "Investment" -> categoryColorMap["trending_up"]
            "bonus", "Bonus" -> categoryColorMap["card_giftcard"]
            else -> Color.Gray
        } ?: Color.Gray
    }

    /**
     * Parses a color string (hex format) to Compose Color.
     * Example: "#FF5722" -> Color(0xFFFF5722)
     *
     * @param colorString The hex color string (with or without #)
     * @return The parsed Color, or Gray if parsing fails
     */
    fun parseColor(colorString: String?): Color {
        if (colorString == null) return Color.Gray
        return try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (e: Exception) {
            Color.Gray
        }
    }

    /**
     * Gets the amount text color based on transaction type.
     *
     * @param isExpense Whether the transaction is an expense
     * @return Red for expense, Green for income
     */
    fun getAmountColor(isExpense: Boolean): Color {
        return if (isExpense) ExpenseColor else IncomeColor
    }

    /**
     * Gets the balance color based on whether it's positive or negative.
     *
     * @param isPositive Whether the balance is positive
     * @return Green for positive, Red for negative
     */
    fun getBalanceColor(isPositive: Boolean): Color {
        return if (isPositive) IncomeColor else ExpenseColor
    }

    /**
     * Gets a color from the pie chart palette by index.
     * Wraps around if index exceeds palette size.
     *
     * @param index The index in the data list
     * @return A color from the pie chart palette
     */
    fun getPieChartColor(index: Int): Color {
        return pieChartColors[index % pieChartColors.size]
    }

    /**
     * Gets the budget progress color based on usage percentage.
     *
     * @param progress Budget usage progress (0.0 to 1.0+)
     * @return Green for < 60%, Orange for 60-80%, Red for > 80%
     */
    fun getBudgetProgressColor(progress: Float): Color {
        return when {
            progress < 0.6f -> IncomeColor   // Green - safe
            progress < 0.8f -> WarningColor  // Orange - warning
            else -> ExpenseColor             // Red - over budget
        }
    }
}
