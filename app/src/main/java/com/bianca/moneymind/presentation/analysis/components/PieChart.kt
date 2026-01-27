package com.bianca.moneymind.presentation.analysis.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bianca.moneymind.presentation.analysis.CategoryAmount
import com.bianca.moneymind.ui.theme.MoneyMindTheme
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Data class for pie chart slice
 */
data class PieSlice(
    val id: String,
    val label: String,
    val value: Double,
    val percentage: Float,
    val color: Color
)

/**
 * Predefined colors for pie chart slices
 */
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
 * Animated Pie Chart with click interaction
 */
@Composable
fun PieChart(
    slices: List<PieSlice>,
    onSliceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedSliceId: String? = null,
    centerLabel: String = "總計"
) {
    if (slices.isEmpty()) {
        Box(
            modifier = modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "無資料",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Animation progress
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    // Track which slice is being hovered/selected
    var clickedAngle by remember { mutableStateOf(-1f) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .pointerInput(slices) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y

                        // Calculate distance from center
                        val distance = sqrt(dx * dx + dy * dy)
                        val radius = size.width / 2f

                        // Check if click is within the pie ring
                        if (distance <= radius && distance >= radius * 0.3f) {
                            // atan2 返回角度：0° 在右邊(3點)，順時針增加（螢幕座標Y向下）
                            // Canvas arc 從 -90°（12點）開始順時針
                            // 轉換：點擊角度 + 90° = Canvas 角度
                            val rawAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            // 轉換為 0-360 範圍，從 12 點鐘方向開始
                            val clickAngle = (rawAngle + 90 + 360) % 360

                            // 找出點擊的是哪個切片
                            var accumulatedAngle = 0f
                            for (slice in slices) {
                                val sweepAngle = slice.percentage / 100f * 360f
                                val sliceStart = accumulatedAngle
                                val sliceEnd = accumulatedAngle + sweepAngle

                                if (clickAngle >= sliceStart && clickAngle < sliceEnd) {
                                    onSliceClick(slice.id)
                                    break
                                }
                                accumulatedAngle += sweepAngle
                            }
                        }
                    }
                }
        ) {
            val strokeWidth = 48.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            var startAngle = -90f

            slices.forEach { slice ->
                val sweepAngle = slice.percentage / 100f * 360f * animationProgress.value
                val isSelected = slice.id == selectedSliceId

                // Draw arc
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(
                        width = if (isSelected) strokeWidth * 1.2f else strokeWidth
                    )
                )

                startAngle += slice.percentage / 100f * 360f * animationProgress.value
            }
        }

        // Center text - show total or selected amount
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displaySlice = slices.find { it.id == selectedSliceId }
            if (displaySlice != null) {
                Text(
                    text = displaySlice.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${String.format("%,.0f", displaySlice.value)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.1f", displaySlice.percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = displaySlice.color
                )
            } else {
                Text(
                    text = centerLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${String.format("%,.0f", slices.sumOf { it.value })}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Legend for pie chart
 */
@Composable
fun PieChartLegend(
    slices: List<PieSlice>,
    onSliceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        slices.forEach { slice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(color = slice.color)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Label
                Text(
                    text = slice.label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                // Amount
                Text(
                    text = "$${String.format("%,.0f", slice.value)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Percentage
                Text(
                    text = "${String.format("%.1f", slice.percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Convert CategoryAmount list to PieSlice list
 */
fun List<CategoryAmount>.toPieSlices(): List<PieSlice> {
    return mapIndexed { index, item ->
        PieSlice(
            id = item.categoryId,
            label = item.categoryName,
            value = item.amount,
            percentage = item.percentage,
            color = pieChartColors[index % pieChartColors.size]
        )
    }
}

// ==================== Preview ====================

@Preview(showBackground = true)
@Composable
private fun PieChartPreview() {
    MoneyMindTheme {
        val slices = listOf(
            PieSlice("food", "餐飲", 4500.0, 36.4f, pieChartColors[0]),
            PieSlice("transport", "交通", 3200.0, 25.9f, pieChartColors[1]),
            PieSlice("shopping", "購物", 2800.0, 22.7f, pieChartColors[2]),
            PieSlice("entertainment", "娛樂", 1850.0, 15.0f, pieChartColors[3])
        )

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PieChart(
                slices = slices,
                onSliceClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            PieChartLegend(
                slices = slices,
                onSliceClick = {}
            )
        }
    }
}
