package com.bianca.moneymind.navigation

sealed class Screen(val route: String) {
    // Bottom Navigation
    data object Home : Screen("home")
    data object Analysis : Screen("analysis")
    data object History : Screen("history")
    data object Settings : Screen("settings")

    // Record Entry
    data object Chat : Screen("chat")
    data object Camera : Screen("camera")
    data object ManualInput : Screen("manual_input")

    // Transaction
    data object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: String) = "edit_transaction/$transactionId"
    }

    // From Analysis
    data object CategoryTransactions : Screen("category_transactions/{categoryId}") {
        fun createRoute(categoryId: String) = "category_transactions/$categoryId"
    }

    // Settings Sub-pages
    data object ManageCategories : Screen("settings/categories")
    data object LearnedRules : Screen("settings/learned_rules")
    data object BudgetSetting : Screen("settings/budget")
    data object ThemeSetting : Screen("settings/theme")
    data object About : Screen("settings/about")
}
