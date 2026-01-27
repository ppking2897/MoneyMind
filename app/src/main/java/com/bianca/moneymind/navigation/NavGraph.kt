package com.bianca.moneymind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import com.bianca.moneymind.presentation.analysis.AnalysisScreen
import com.bianca.moneymind.presentation.camera.CameraScreen
import com.bianca.moneymind.presentation.categorytransactions.CategoryTransactionsScreen
import com.bianca.moneymind.presentation.chat.ChatScreen
import com.bianca.moneymind.presentation.edit.EditTransactionScreen
import com.bianca.moneymind.presentation.history.HistoryScreen
import com.bianca.moneymind.presentation.home.HomeScreen
import com.bianca.moneymind.presentation.manual.ManualInputScreen
import com.bianca.moneymind.presentation.settings.SettingsScreen
import com.bianca.moneymind.presentation.settings.about.AboutScreen
import com.bianca.moneymind.presentation.settings.budget.BudgetSettingScreen
import com.bianca.moneymind.presentation.settings.categories.ManageCategoriesScreen
import com.bianca.moneymind.presentation.settings.rules.LearnedRulesScreen
import com.bianca.moneymind.presentation.settings.theme.ThemeSettingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    bottomPadding: Dp = 0.dp
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Bottom Navigation Screens (需要 bottom padding)
        val bottomNavModifier = Modifier.padding(bottom = bottomPadding)

        composable(Screen.Home.route) {
            HomeScreen(
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                },
                modifier = bottomNavModifier
            )
        }

        composable(Screen.Analysis.route) {
            AnalysisScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Screen.CategoryTransactions.createRoute(categoryId))
                },
                modifier = bottomNavModifier
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                },
                modifier = bottomNavModifier
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
                },
                modifier = bottomNavModifier
            )
        }

        // Record Entry Screens
        composable(Screen.ManualInput.route) {
            ManualInputScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.Chat.route) { backStackEntry ->
            // Observe result from CameraScreen
            val savedAmount = backStackEntry.savedStateHandle.get<Double>("receipt_amount")
            val savedDescription = backStackEntry.savedStateHandle.get<String>("receipt_description")

            ChatScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                receiptSavedAmount = savedAmount,
                receiptSavedDescription = savedDescription,
                onReceiptResultHandled = {
                    backStackEntry.savedStateHandle.remove<Double>("receipt_amount")
                    backStackEntry.savedStateHandle.remove<String>("receipt_description")
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionSaved = { amount, description ->
                    // Pass result back to ChatScreen
                    navController.previousBackStackEntry?.savedStateHandle?.set("receipt_amount", amount)
                    navController.previousBackStackEntry?.savedStateHandle?.set("receipt_description", description)
                    navController.popBackStack()
                }
            )
        }

        // Transaction Edit
        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) {
            EditTransactionScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionUpdated = { navController.popBackStack() },
                onTransactionDeleted = { navController.popBackStack() }
            )
        }

        // Category Transactions
        composable(
            route = Screen.CategoryTransactions.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) {
            CategoryTransactionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                }
            )
        }

        // Settings Sub-screens
        composable(Screen.BudgetSetting.route) {
            BudgetSettingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ThemeSetting.route) {
            ThemeSettingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageCategories.route) {
            ManageCategoriesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LearnedRules.route) {
            LearnedRulesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    title: String,
    message: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
