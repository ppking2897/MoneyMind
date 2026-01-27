package com.bianca.moneymind.presentation.analysis.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.time.YearMonth

/**
 * Data class for monthly expense trend
 */
data class MonthlyTrendData(
    val month: YearMonth,
    val expense: Double,
    val income: Double
)

/**
 * Bar chart showing monthly expense trends
 * 只顯示每月支出趨勢，點擊可查看該月的收支明細
 */
@Composable
fun MonthlyTrendChart(
    data: List<MonthlyTrendData>,
    modifier: Modifier = Modifier,
    selectedMonth: YearMonth? = null
) {
    if (data.isEmpty()) {
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    val expenseColor = Color(0xFFE53935)
    val incomeColor = Color(0xFF43A047)

    // 預設選中當月的資料
    var selectedData by remember(data, selectedMonth) {
        val defaultSelected = selectedMonth?.let { month ->
            data.find { it.month == month }
        }
        mutableStateOf(defaultSelected)
    }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                series(data.map { it.expense })
            }
        }
        // 不重置 selectedData，保持預設選中狀態
    }

    // 簡化的 Marker，只偵測點擊位置
    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            color = Color.Transparent,
            textSize = MaterialTheme.typography.bodySmall.fontSize,
            padding = com.patrykandpatrick.vico.core.common.Dimensions(0f),
            background = null
        ),
        valueFormatter = { _, targets ->
            val target = targets.firstOrNull()
            if (target != null) {
                val index = kotlin.math.round(target.x).toInt()
                if (index in data.indices) {
                    selectedData = data[index]
                }
            }
            " "  // 不能返回空字串
        }
    )

    Column(modifier = modifier) {
        // 選中資料的資訊卡片（顯示該月完整收支）
        AnimatedVisibility(visible = selectedData != null) {
            selectedData?.let { selected ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { selectedData = null },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selected.month.year}年${selected.month.monthValue}月",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "點擊關閉",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "支出",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$${String.format("%,d", selected.expense.toInt())}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = expenseColor
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "收入",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$${String.format("%,d", selected.income.toInt())}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = incomeColor
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "結餘",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val balance = selected.income - selected.expense
                                Text(
                                    text = "${if (balance >= 0) "+" else ""}$${String.format("%,d", balance.toInt())}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (balance >= 0) incomeColor else expenseColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // 圖表
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = fill(expenseColor),
                            shape = CorneredShape.rounded(allPercent = 20),
                            thickness = 12.dp  // 年視圖只有 12 根柱子，用較細的寬度
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = { _, value, _ ->
                        val index = value.toInt().coerceIn(0, data.lastIndex.coerceAtLeast(0))
                        if (data.isNotEmpty() && index in data.indices) {
                            val month = data[index].month
                            "${month.monthValue}月"
                        } else {
                            "-"  // 不能返回空字串
                        }
                    }
                ),
                marker = marker
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}
