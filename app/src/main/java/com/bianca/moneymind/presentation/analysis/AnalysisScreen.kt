package com.bianca.moneymind.presentation.analysis

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import java.time.YearMonth
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bianca.moneymind.presentation.analysis.components.DailyExpenseChart
import com.bianca.moneymind.presentation.analysis.components.MonthlyTrendChart
import com.bianca.moneymind.presentation.analysis.components.PieChart
import com.bianca.moneymind.presentation.analysis.components.PieChartLegend
import com.bianca.moneymind.presentation.analysis.components.toPieSlices
import com.bianca.moneymind.ui.theme.MoneyMindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分析", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        AnalysisContent(
            uiState = uiState,
            onTimeRangeChange = viewModel::onTimeRangeChange,
            onMonthChange = viewModel::onMonthChange,
            onYearChange = viewModel::onYearChange,
            onCategoryClick = onCategoryClick,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// 圓餅圖顯示類型
enum class PieChartType {
    EXPENSE, INCOME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalysisContent(
    uiState: AnalysisUiState,
    onTimeRangeChange: (TimeRange) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onYearChange: (Int) -> Unit,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track selected slice in pie chart
    var selectedSliceId by remember { mutableStateOf<String?>(null) }
    // Track pie chart type (expense or income)
    var pieChartType by remember { mutableStateOf(PieChartType.EXPENSE) }
    // Dialog states
    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    // Show dialogs
    if (showMonthPicker) {
        YearMonthPickerDialog(
            selectedYearMonth = uiState.selectedMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = { yearMonth ->
                onMonthChange(yearMonth)
                showMonthPicker = false
            }
        )
    }

    if (showYearPicker) {
        YearPickerDialog(
            selectedYear = uiState.selectedYear,
            onDismiss = { showYearPicker = false },
            onConfirm = { year ->
                onYearChange(year)
                showYearPicker = false
            }
        )
    }

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
        // Time Range Selector
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TimeRange.entries.forEachIndexed { index, timeRange ->
                        SegmentedButton(
                            selected = uiState.selectedTimeRange == timeRange,
                            onClick = { onTimeRangeChange(timeRange) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = TimeRange.entries.size
                            )
                        ) {
                            Text(
                                when (timeRange) {
                                    TimeRange.MONTH -> "月"
                                    TimeRange.YEAR -> "年"
                                }
                            )
                        }
                    }
                }

                // Time filter button (month or year picker)
                when (uiState.selectedTimeRange) {
                    TimeRange.MONTH -> {
                        TimeFilterButton(
                            text = "${uiState.selectedMonth.year}年${uiState.selectedMonth.monthValue}月",
                            onClick = { showMonthPicker = true }
                        )
                    }
                    TimeRange.YEAR -> {
                        TimeFilterButton(
                            text = "${uiState.selectedYear}年",
                            onClick = { showYearPicker = true }
                        )
                    }
                }
            }
        }

        // Summary Card
        item {
            SummaryCard(
                timeRange = uiState.selectedTimeRange,
                totalExpense = uiState.totalExpense,
                totalIncome = uiState.totalIncome,
                balance = uiState.balance,
                isPositiveBalance = uiState.isPositiveBalance
            )
        }

        // Monthly Trend Chart - 一直顯示月度支出趨勢
        if (uiState.monthlyTrend.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "月度支出趨勢",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        MonthlyTrendChart(
                            data = uiState.monthlyTrend,
                            selectedMonth = uiState.selectedMonth,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Daily Chart (for non-Year views) - 只顯示支出趨勢（固定顯示）
        if (uiState.selectedTimeRange != TimeRange.YEAR) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "每日支出趨勢",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        DailyExpenseChart(
                            data = uiState.dailyExpenses,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Pie Chart with toggle
        if (uiState.categoryExpenses.isNotEmpty() || uiState.categoryIncomes.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Toggle for expense/income
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            SegmentedButton(
                                selected = pieChartType == PieChartType.EXPENSE,
                                onClick = {
                                    pieChartType = PieChartType.EXPENSE
                                    selectedSliceId = null
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("支出分佈")
                            }
                            SegmentedButton(
                                selected = pieChartType == PieChartType.INCOME,
                                onClick = {
                                    pieChartType = PieChartType.INCOME
                                    selectedSliceId = null
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("收入分佈")
                            }
                        }

                        val currentData = if (pieChartType == PieChartType.EXPENSE) {
                            uiState.categoryExpenses
                        } else {
                            uiState.categoryIncomes
                        }

                        if (currentData.isNotEmpty()) {
                            PieChart(
                                slices = currentData.toPieSlices(),
                                onSliceClick = { categoryId ->
                                    selectedSliceId = if (selectedSliceId == categoryId) null else categoryId
                                },
                                selectedSliceId = selectedSliceId,
                                centerLabel = if (pieChartType == PieChartType.EXPENSE) "總支出" else "總收入",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // 圓餅圖圖例
                            Spacer(modifier = Modifier.height(12.dp))
                            PieChartLegend(
                                slices = currentData.toPieSlices(),
                                onSliceClick = { categoryId ->
                                    selectedSliceId = if (selectedSliceId == categoryId) null else categoryId
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        } else {
                            Text(
                                text = if (pieChartType == PieChartType.EXPENSE) "尚無支出記錄" else "尚無收入記錄",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 32.dp)
                            )
                        }
                    }
                }
            }
        }

        // Category List Header
        item {
            Text(
                text = if (pieChartType == PieChartType.EXPENSE) "支出明細" else "收入明細",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Category List (based on selected pie chart type)
        val currentCategoryData = if (pieChartType == PieChartType.EXPENSE) {
            uiState.categoryExpenses
        } else {
            uiState.categoryIncomes
        }

        if (currentCategoryData.isEmpty()) {
            item {
                Text(
                    text = if (pieChartType == PieChartType.EXPENSE) "尚無支出記錄" else "尚無收入記錄",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(currentCategoryData, key = { it.categoryId }) { categoryItem ->
                CategoryAmountItem(
                    categoryAmount = categoryItem,
                    color = currentCategoryData.toPieSlices()
                        .find { it.id == categoryItem.categoryId }?.color
                        ?: MaterialTheme.colorScheme.primary,
                    onClick = { onCategoryClick(categoryItem.categoryId) }
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SummaryCard(
    timeRange: TimeRange,
    totalExpense: Double,
    totalIncome: Double,
    balance: Double,
    isPositiveBalance: Boolean
) {
    val periodLabel = when (timeRange) {
        TimeRange.MONTH -> "本月"
        TimeRange.YEAR -> "本年"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${periodLabel}支出 vs 收入",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("支出", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "$${String.format("%,.0f", totalExpense)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("收入", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "$${String.format("%,.0f", totalIncome)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF43A047)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("結餘", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${if (isPositiveBalance) "+" else ""}$${String.format("%,.0f", balance)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositiveBalance) Color(0xFF43A047) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryAmountItem(
    categoryAmount: CategoryAmount,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Color indicator
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.size(12.dp)
                ) {
                    drawCircle(color = color)
                }
                Text(
                    text = categoryAmount.categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${String.format("%,.0f", categoryAmount.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.0f", categoryAmount.percentage)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==================== Time Picker Dialogs ====================

/**
 * 時間選擇按鈕 - 顯示當前選擇的時間（年月或年）
 */
@Composable
private fun TimeFilterButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(text = text)
            }
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * 年月選擇器對話框
 */
@Composable
private fun YearMonthPickerDialog(
    selectedYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    var tempYear by remember { mutableIntStateOf(selectedYearMonth.year) }
    var tempMonth by remember { mutableIntStateOf(selectedYearMonth.monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "選擇月份",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Year selector with arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { tempYear-- }) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "上一年"
                        )
                    }
                    Text(
                        text = "${tempYear}年",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(80.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { tempYear++ }) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "下一年"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month grid (4 columns x 3 rows)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(160.dp)
                ) {
                    items((1..12).toList()) { month ->
                        val isSelected = month == tempMonth
                        Surface(
                            onClick = { tempMonth = month },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = if (isSelected) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "${month}月",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(YearMonth.of(tempYear, tempMonth)) }) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 年份選擇器對話框
 */
@Composable
private fun YearPickerDialog(
    selectedYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val currentYear = java.time.Year.now().value
    // Show years from 5 years ago to 2 years in the future
    val years = ((currentYear - 5)..(currentYear + 2)).toList()
    var tempYear by remember { mutableIntStateOf(selectedYear) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "選擇年份",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(180.dp)
            ) {
                items(years) { year ->
                    val isSelected = year == tempYear
                    Surface(
                        onClick = { tempYear = year },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        border = if (isSelected) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "${year}年",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempYear) }) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// ==================== Preview ====================

@Preview(showBackground = true)
@Composable
private fun AnalysisContentPreview() {
    MoneyMindTheme {
        AnalysisContent(
            uiState = AnalysisUiState.mock(),
            onTimeRangeChange = {},
            onMonthChange = {},
            onYearChange = {},
            onCategoryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalysisContentLoadingPreview() {
    MoneyMindTheme {
        AnalysisContent(
            uiState = AnalysisUiState(isLoading = true),
            onTimeRangeChange = {},
            onMonthChange = {},
            onYearChange = {},
            onCategoryClick = {}
        )
    }
}
