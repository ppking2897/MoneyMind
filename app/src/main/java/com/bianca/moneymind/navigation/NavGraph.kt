package com.bianca.moneymind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bianca.moneymind.presentation.analysis.AnalysisScreen
import com.bianca.moneymind.presentation.history.HistoryScreen
import com.bianca.moneymind.presentation.home.HomeScreen
import com.bianca.moneymind.presentation.manual.ManualInputScreen
import com.bianca.moneymind.presentation.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Bottom Navigation Screens
        composable(Screen.Home.route) {
            HomeScreen(
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                }
            )
        }

        composable(Screen.Analysis.route) {
            AnalysisScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Screen.CategoryTransactions.createRoute(categoryId))
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToCategories = {
                    navController.navigate(Screen.ManageCategories.route)
                },
                onNavigateToRules = {
                    navController.navigate(Screen.LearnedRules.route)
                },
                onNavigateToBudget = {
                    navController.navigate(Screen.BudgetSetting.route)
                },
                onNavigateToTheme = {
                    navController.navigate(Screen.ThemeSetting.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        // Record Entry Screens
        composable(Screen.ManualInput.route) {
            ManualInputScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionSaved = { navController.popBackStack() }
            )
        }

        // Transaction Edit
        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: return@composable
            // EditTransactionScreen will be added later
        }

        // Category Transactions
        composable(
            route = Screen.CategoryTransactions.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
            // CategoryTransactionsScreen will be added later
        }
    }
}
