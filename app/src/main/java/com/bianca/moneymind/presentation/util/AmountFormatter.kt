package com.bianca.moneymind.presentation.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility object for formatting and parsing monetary amounts.
 * Provides consistent amount display across the app.
 */
object AmountFormatter {

    private val decimalFormat = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
    private val decimalFormatWithDecimals = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))

    /**
     * Formats a monetary amount with thousand separators.
     * Example: 1234567.89 -> "1,234,568"
     *
     * @param amount The amount to format
     * @param showDecimals Whether to show decimal places (default: false)
     * @return Formatted amount string with thousand separators
     */
    fun formatAmount(amount: Double, showDecimals: Boolean = false): String {
        return if (showDecimals) {
            decimalFormatWithDecimals.format(amount)
        } else {
            decimalFormat.format(amount)
        }
    }

    /**
     * Formats a monetary amount with currency symbol.
     * Example: 1234567.89 -> "$1,234,568"
     *
     * @param amount The amount to format
     * @param currencySymbol The currency symbol to prepend (default: "$")
     * @param showDecimals Whether to show decimal places (default: false)
     * @return Formatted amount string with currency symbol
     */
    fun formatAmountWithCurrency(
        amount: Double,
        currencySymbol: String = "$",
        showDecimals: Boolean = false
    ): String {
        return "$currencySymbol${formatAmount(amount, showDecimals)}"
    }

    /**
     * Formats a monetary amount with sign (+ or -) and currency symbol.
     * For expenses (isExpense = true): "-$1,234"
     * For income (isExpense = false): "+$1,234"
     *
     * @param amount The absolute amount to format
     * @param isExpense Whether this is an expense (negative) or income (positive)
     * @param currencySymbol The currency symbol to use (default: "$")
     * @param showDecimals Whether to show decimal places (default: false)
     * @return Formatted amount string with sign and currency symbol
     */
    fun formatAmountWithSign(
        amount: Double,
        isExpense: Boolean,
        currencySymbol: String = "$",
        showDecimals: Boolean = false
    ): String {
        val prefix = if (isExpense) "-" else "+"
        return "$prefix$currencySymbol${formatAmount(amount, showDecimals)}"
    }

    /**
     * Formats a balance amount with sign.
     * Positive balance: "+$1,234"
     * Negative balance: "-$1,234"
     *
     * @param balance The balance amount (can be positive or negative)
     * @param currencySymbol The currency symbol to use (default: "$")
     * @param showDecimals Whether to show decimal places (default: false)
     * @return Formatted balance string with appropriate sign
     */
    fun formatBalance(
        balance: Double,
        currencySymbol: String = "$",
        showDecimals: Boolean = false
    ): String {
        val prefix = if (balance >= 0) "+" else ""
        return "$prefix$currencySymbol${formatAmount(balance, showDecimals)}"
    }

    /**
     * Parses a formatted amount string back to a Double.
     * Handles strings with currency symbols, thousand separators, and signs.
     * Example: "$1,234.56" -> 1234.56
     * Example: "-$1,234" -> -1234.0
     *
     * @param input The formatted amount string to parse
     * @return The parsed amount as Double, or null if parsing fails
     */
    fun parseAmount(input: String): Double? {
        return try {
            // Remove currency symbols and whitespace
            val cleaned = input
                .replace(Regex("[$$\\s]"), "")
                .replace(",", "")
                .trim()

            if (cleaned.isEmpty()) {
                null
            } else {
                cleaned.toDoubleOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Formats percentage value.
     * Example: 75.5 -> "75%"
     *
     * @param percentage The percentage value (0-100)
     * @param showDecimals Whether to show decimal places (default: false)
     * @return Formatted percentage string
     */
    fun formatPercentage(percentage: Double, showDecimals: Boolean = false): String {
        return if (showDecimals) {
            "${String.format(Locale.US, "%.1f", percentage)}%"
        } else {
            "${String.format(Locale.US, "%.0f", percentage)}%"
        }
    }
}
