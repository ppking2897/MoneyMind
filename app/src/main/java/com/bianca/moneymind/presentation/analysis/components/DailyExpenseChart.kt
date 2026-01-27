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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Data class for daily expense
 */
data class DailyExpenseData(
    val date: LocalDate,
    val amount: Double
)

/**
 * Bar chart showing daily expenses only
 * 只顯示每日支出趨勢（紅色柱狀圖）
 * 收入資訊在 SummaryCard 和圓餅圖中顯示
 * 固定顯示整個月份，預設選中今天
 */
@Composable
fun DailyExpenseChart(
    data: List<DailyExpenseData>,
    modifier: Modifier = Modifier
) {
    // 如果沒有資料，顯示空狀態提示
    if (data.isEmpty()) {
        Text(
            text = "尚無支出資料",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 32.dp)
        )
        return
    }

    val allDates = data.map { it.date }.sorted()
    val expenseMap = data.associateBy { it.date }

    val modelProducer = remember { CartesianChartModelProducer() }

    val expenseColor = Color(0xFFE53935)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M月d日") }

    // 找到今天在資料中的索引，預設選中今天
    val today = LocalDate.now()
    val defaultIndex = remember(allDates) {
        // 優先找今天，找不到就找最後一個有資料（金額>0）的日子
        val todayIdx = allDates.indexOfFirst { it == today }
        if (todayIdx >= 0) {
            todayIdx
        } else {
            // 找最後一個金額 > 0 的日子
            val lastWithData = allDates.indexOfLast { date ->
                (expenseMap[date]?.amount ?: 0.0) > 0
            }
            if (lastWithData >= 0) lastWithData else 0
        }
    }

    var selectedIndex by remember(data) { mutableStateOf<Int?>(defaultIndex) }

    // Touch 事件偵測
    var markerIndex by remember { mutableIntStateOf(-1) }      // Marker 追蹤的索引
    var touchDownIndex by remember { mutableIntStateOf(-1) }   // 按下時的索引
    var isPressed by remember { mutableStateOf(false) }        // 是否正在按壓
    var hasMoved by remember { mutableStateOf(false) }         // 是否移動過

    // 滾動狀態（需要在 LaunchedEffect 之前宣告）
    val scrollState = rememberVicoScrollState()

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                series(allDates.map { expenseMap[it]?.amount ?: 0.0 })
            }
        }
    }

    // 圖表載入後滾動到今天的位置
    LaunchedEffect(defaultIndex, allDates.size) {
        if (allDates.isNotEmpty() && defaultIndex > 0) {
            delay(150)  // 等待圖表渲染完成
            scrollState.scroll(Scroll.Absolute.x(defaultIndex.toDouble()))
        }
    }

    // Marker 負責追蹤手指位置，並在按壓時記錄起始索引
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
                if (index in allDates.indices) {
                    // 如果正在按壓且還沒記錄起始索引，記錄它
                    if (isPressed && touchDownIndex < 0) {
                        touchDownIndex = index
                    }
                    // 如果索引改變了，標記為已移動
                    if (isPressed && touchDownIndex >= 0 && index != touchDownIndex) {
                        hasMoved = true
                    }
                    markerIndex = index
                }
            }
            " "
        }
    )

    // 處理 Touch 事件的 Modifier
    val touchModifier = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                when (event.type) {
                    PointerEventType.Press -> {
                        // ACTION_DOWN：標記開始按壓，索引由 Marker 設定
                        isPressed = true
                        touchDownIndex = -1  // 等待 Marker 設定
                        hasMoved = false
                    }
                    PointerEventType.Move -> {
                        // ACTION_MOVE：如果移動到不同的柱子，標記為拖曳
                        if (isPressed && touchDownIndex >= 0 && markerIndex != touchDownIndex) {
                            hasMoved = true
                        }
                    }
                    PointerEventType.Release -> {
                        // ACTION_UP：如果沒有移動過，就是點擊
                        if (isPressed && !hasMoved && touchDownIndex >= 0) {
                            if (touchDownIndex in allDates.indices) {
                                selectedIndex = touchDownIndex
                            }
                        }
                        isPressed = false
                        touchDownIndex = -1
                        hasMoved = false
                    }
                    else -> {}
                }
            }
        }
    }

    Column(modifier = modifier) {
        // 選中資料的資訊卡片
        AnimatedVisibility(visible = selectedIndex != null) {
            selectedIndex?.let { idx ->
                if (idx in allDates.indices) {
                    val date = allDates[idx]
                    val expense = expenseMap[date]?.amount ?: 0.0
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { selectedIndex = null },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = date.format(dateFormatter),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$${String.format("%,.0f", expense)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = expenseColor
                                )
                            }
                            Text(
                                text = "點擊關閉",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 圖表（自動滾動到今天位置）
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = fill(expenseColor),
                            shape = CorneredShape.rounded(allPercent = 20),
                            thickness = 16.dp
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = { _, value, _ ->
                        val index = value.toInt().coerceIn(0, allDates.lastIndex.coerceAtLeast(0))
                        if (allDates.isNotEmpty() && index in allDates.indices) {
                            val date = allDates[index]
                            val showMonth = index == 0 ||
                                (index > 0 && allDates[index - 1].month != date.month)
                            if (showMonth) {
                                "${date.monthValue}/${date.dayOfMonth}"
                            } else {
                                "${date.dayOfMonth}"
                            }
                        } else {
                            "-"  // 不能返回空字串
                        }
                    }
                ),
                marker = marker
            ),
            modelProducer = modelProducer,
            scrollState = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .then(touchModifier)
        )
    }
}
