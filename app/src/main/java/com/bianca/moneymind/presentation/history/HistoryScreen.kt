package com.bianca.moneymind.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.presentation.components.EmptyStateView
import com.bianca.moneymind.presentation.home.TransactionWithCategory
import com.bianca.moneymind.ui.theme.MoneyMindTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("歷史", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(innerPadding)
        ) {
            HistoryContent(
                uiState = uiState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onFilterChange = viewModel::onFilterChange,
                onTransactionClick = onTransactionClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryContent(
    uiState: HistoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (TransactionFilter) -> Unit,
    onTransactionClick: (String) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("搜尋交易...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜尋") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Filter Tabs
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TransactionFilter.entries.forEachIndexed { index, filter ->
                    SegmentedButton(
                        selected = uiState.selectedFilter == filter,
                        onClick = { onFilterChange(filter) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TransactionFilter.entries.size
                        )
                    ) {
                        Text(
                            when (filter) {
                                TransactionFilter.ALL -> "全部"
                                TransactionFilter.EXPENSE -> "支出"
                                TransactionFilter.INCOME -> "收入"
                            }
                        )
                    }
                }
            }
        }

        // Transaction List by Date
        if (uiState.dailyTransactions.isEmpty()) {
            item {
                EmptyStateView(
                    title = "找不到符合條件的交易",
                    description = if (uiState.searchQuery.isNotEmpty()) "請嘗試其他搜尋關鍵字" else null,
                    icon = Icons.Default.SearchOff
                )
            }
        } else {
            uiState.dailyTransactions.forEach { daily ->
                item {
                    DateHeader(
                        date = daily.date,
                        dailyTotal = daily.dailyTotal
                    )
                }
                items(daily.transactionsWithCategory, key = { it.transaction.id }) { txWithCategory ->
                    TransactionItem(
                        transactionWithCategory = txWithCategory,
                        onClick = { onTransactionClick(txWithCategory.transaction.id) }
                    )
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    dailyTotal: Double
) {
    val today = LocalDate.now()
    val dateText = when (date) {
        today -> "今天"
        today.minusDays(1) -> "昨天"
        else -> date.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))
    }

    val totalColor = if (dailyTotal >= 0) Color(0xFF43A047) else MaterialTheme.colorScheme.error
    val totalPrefix = if (dailyTotal >= 0) "+" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "$totalPrefix$${String.format("%,.0f", dailyTotal)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = totalColor
        )
    }
}

@Composable
private fun TransactionItem(
    transactionWithCategory: TransactionWithCategory,
    onClick: () -> Unit
) {
    val transaction = transactionWithCategory.transaction
    val category = transactionWithCategory.category

    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF43A047)
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

// ==================== Preview ====================

@Preview(showBackground = true)
@Composable
private fun HistoryContentPreview() {
    MoneyMindTheme {
        HistoryContent(
            uiState = HistoryUiState.mock(),
            onSearchQueryChange = {},
            onFilterChange = {},
            onTransactionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryContentLoadingPreview() {
    MoneyMindTheme {
        HistoryContent(
            uiState = HistoryUiState(isLoading = true),
            onSearchQueryChange = {},
            onFilterChange = {},
            onTransactionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryContentEmptyPreview() {
    MoneyMindTheme {
        HistoryContent(
            uiState = HistoryUiState(isLoading = false),
            onSearchQueryChange = {},
            onFilterChange = {},
            onTransactionClick = {}
        )
    }
}
