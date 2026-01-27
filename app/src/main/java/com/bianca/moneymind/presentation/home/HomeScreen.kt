package com.bianca.moneymind.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.TransactionType
import java.time.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import com.bianca.moneymind.ui.theme.MoneyMindTheme
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTransactionClick: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("首頁", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        HomeContent(
            uiState = uiState,
            onTransactionClick = onTransactionClick,
            onPeriodSelected = viewModel::onPeriodSelected,
            onNavigateToSettings = onNavigateToSettings,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onTransactionClick: (String) -> Unit,
    onPeriodSelected: (TimePeriod) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Period Selector
        item {
            TimePeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = onPeriodSelected
            )
        }

        // Budget Card or Budget Setup Prompt
        item {
            if (uiState.hasBudgetSet && uiState.budget > 0) {
                BudgetCard(
                    periodLabel = uiState.periodLabel,
                    periodExpense = uiState.periodExpense,
                    budget = uiState.budget,
                    progress = uiState.budgetProgress,
                    percentage = uiState.budgetPercentage
                )
            } else {
                BudgetSetupPrompt(
                    onSetupClick = onNavigateToSettings
                )
            }
        }

        // Transactions by Date
        if (uiState.transactionsWithCategory.isEmpty()) {
            item {
                EmptyState()
            }
        } else {
            uiState.transactionsWithCategory.forEach { (date, transactionsWithCategory) ->
                item {
                    DateHeader(date = date)
                }
                items(transactionsWithCategory, key = { it.transaction.id }) { txWithCategory ->
                    TransactionItem(
                        transactionWithCategory = txWithCategory,
                        onClick = { onTransactionClick(txWithCategory.transaction.id) }
                    )
                }
            }
        }

        // Bottom spacing for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimePeriod.entries.forEach { period ->
            FilterChip(
                selected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun BudgetCard(
    periodLabel: String,
    periodExpense: Double,
    budget: Double,
    progress: Float,
    percentage: Int
) {
    val progressColor = when {
        progress < 0.6f -> Color(0xFF43A047)  // Green
        progress < 0.8f -> Color(0xFFFFA000)  // Orange
        else -> Color(0xFFE53935)              // Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${periodLabel}支出",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${String.format("%,.0f", periodExpense)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "預算 $${String.format("%,.0f", budget)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodySmall,
                    color = progressColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate) {
    val today = LocalDate.now()
    val displayText = when (date) {
        today -> "今日"
        today.minusDays(1) -> "昨日"
        else -> date.format(DateTimeFormatter.ofPattern("M月d日"))
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun TransactionItem(
    transactionWithCategory: TransactionWithCategory,
    onClick: () -> Unit
) {
    val transaction = transactionWithCategory.transaction
    val category = transactionWithCategory.category

    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) {
        MaterialTheme.colorScheme.error
    } else {
        Color(0xFF43A047)
    }
    val amountPrefix = if (isExpense) "-" else "+"

    // Category color
    val categoryColor = category?.color?.let { parseColor(it) }
        ?: MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(categoryColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category?.icon),
                    contentDescription = category?.name,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Description and Category Name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = category?.name ?: "未分類",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = "$amountPrefix$${String.format("%,.0f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

/**
 * Parse color string (e.g., "#FF5722") to Color
 */
private fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * Get Material Icon for category
 */
private fun getCategoryIcon(iconName: String?): ImageVector {
    return when (iconName) {
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_bag" -> Icons.Default.ShoppingBag
        "home" -> Icons.Default.Home
        "sports_esports" -> Icons.Default.SportsEsports
        "local_hospital" -> Icons.Default.LocalHospital
        "school" -> Icons.Default.School
        "more_horiz" -> Icons.Default.MoreHoriz
        "work" -> Icons.Default.Work
        "trending_up" -> Icons.Default.TrendingUp
        "card_giftcard" -> Icons.Default.CardGiftcard
        "attach_money" -> Icons.Default.AttachMoney
        else -> Icons.Default.Category
    }
}

@Composable
private fun BudgetSetupPrompt(
    onSetupClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onSetupClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "設定每月預算",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "追蹤支出，讓記帳更有目標",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "尚無交易記錄",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "點擊下方 + 開始記帳",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== Preview ====================

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    MoneyMindTheme {
        HomeContent(
            uiState = HomeUiState.mock(),
            onTransactionClick = {},
            onPeriodSelected = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentLoadingPreview() {
    MoneyMindTheme {
        HomeContent(
            uiState = HomeUiState(isLoading = true),
            onTransactionClick = {},
            onPeriodSelected = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentEmptyPreview() {
    MoneyMindTheme {
        HomeContent(
            uiState = HomeUiState(isLoading = false),
            onTransactionClick = {},
            onPeriodSelected = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentNoBudgetPreview() {
    MoneyMindTheme {
        HomeContent(
            uiState = HomeUiState(isLoading = false, hasBudgetSet = false, budget = 0.0),
            onTransactionClick = {},
            onPeriodSelected = {},
            onNavigateToSettings = {}
        )
    }
}
