package com.bianca.moneymind.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bianca.moneymind.presentation.util.CategoryColorMapper
import com.bianca.moneymind.ui.theme.MoneyMindTheme

/**
 * Maps icon name strings to Material Icon vectors.
 *
 * Supported icon names:
 * - Expense: restaurant, directions_car, shopping_bag, home, sports_esports, local_hospital, school, more_horiz
 * - Income: work, trending_up, card_giftcard, attach_money
 *
 * @param iconName The icon name string from the category
 * @return The corresponding ImageVector, or Category icon as default
 */
fun getCategoryIcon(iconName: String?): ImageVector {
    return when (iconName?.lowercase()) {
        // Expense category icons
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_bag" -> Icons.Default.ShoppingBag
        "home" -> Icons.Default.Home
        "sports_esports" -> Icons.Default.SportsEsports
        "local_hospital" -> Icons.Default.LocalHospital
        "school" -> Icons.Default.School
        "more_horiz" -> Icons.Default.MoreHoriz

        // Income category icons
        "work" -> Icons.Default.Work
        "trending_up" -> Icons.Default.TrendingUp
        "card_giftcard" -> Icons.Default.CardGiftcard
        "attach_money" -> Icons.Default.AttachMoney

        // Default
        else -> Icons.Default.Category
    }
}

/**
 * A composable that displays a category icon.
 * Can be displayed as just the icon or inside a colored circle background.
 *
 * @param iconName The icon name string (e.g., "restaurant", "directions_car")
 * @param modifier Modifier for the composable
 * @param size The size of the icon (default: 24.dp)
 * @param tint The tint color for the icon (default: current content color)
 * @param contentDescription Accessibility description for the icon
 */
@Composable
fun CategoryIcon(
    iconName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String? = null
) {
    Icon(
        imageVector = getCategoryIcon(iconName),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}

/**
 * A composable that displays a category icon inside a colored circular background.
 * The background color is determined by the icon name using CategoryColorMapper,
 * or can be overridden with a custom color.
 *
 * @param iconName The icon name string (e.g., "restaurant", "directions_car")
 * @param modifier Modifier for the composable
 * @param containerSize The size of the circular container (default: 40.dp)
 * @param iconSize The size of the icon inside the container (default: 24.dp)
 * @param backgroundColor The background color of the circle. If null, uses CategoryColorMapper.
 * @param iconTint The tint color for the icon (default: White)
 * @param contentDescription Accessibility description for the icon
 */
@Composable
fun CategoryIconWithBackground(
    iconName: String?,
    modifier: Modifier = Modifier,
    containerSize: Dp = 40.dp,
    iconSize: Dp = 24.dp,
    backgroundColor: Color? = null,
    iconTint: Color = Color.White,
    contentDescription: String? = null
) {
    val bgColor = backgroundColor ?: CategoryColorMapper.getCategoryColor(iconName)

    Box(
        modifier = modifier
            .size(containerSize)
            .background(bgColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getCategoryIcon(iconName),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * A composable for displaying category icon with parsed color from hex string.
 * Useful when the category has a custom color stored as hex string.
 *
 * @param iconName The icon name string
 * @param colorHex The hex color string (e.g., "#FF5722")
 * @param modifier Modifier for the composable
 * @param containerSize The size of the circular container (default: 40.dp)
 * @param iconSize The size of the icon inside the container (default: 24.dp)
 * @param iconTint The tint color for the icon (default: White)
 * @param contentDescription Accessibility description for the icon
 */
@Composable
fun CategoryIconWithHexColor(
    iconName: String?,
    colorHex: String?,
    modifier: Modifier = Modifier,
    containerSize: Dp = 40.dp,
    iconSize: Dp = 24.dp,
    iconTint: Color = Color.White,
    contentDescription: String? = null
) {
    val bgColor = colorHex?.let { CategoryColorMapper.parseColor(it) }
        ?: CategoryColorMapper.getCategoryColor(iconName)

    CategoryIconWithBackground(
        iconName = iconName,
        modifier = modifier,
        containerSize = containerSize,
        iconSize = iconSize,
        backgroundColor = bgColor,
        iconTint = iconTint,
        contentDescription = contentDescription
    )
}

// ==================== Previews ====================

@Preview(showBackground = true)
@Composable
private fun CategoryIconPreview() {
    MoneyMindTheme {
        CategoryIcon(
            iconName = "restaurant",
            size = 32.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryIconWithBackgroundPreview() {
    MoneyMindTheme {
        CategoryIconWithBackground(
            iconName = "restaurant",
            containerSize = 48.dp,
            iconSize = 28.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryIconWithHexColorPreview() {
    MoneyMindTheme {
        CategoryIconWithHexColor(
            iconName = "shopping_bag",
            colorHex = "#E91E63",
            containerSize = 48.dp,
            iconSize = 28.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryIconsGridPreview() {
    MoneyMindTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            // Expense icons
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                CategoryIconWithBackground(iconName = "restaurant")
                CategoryIconWithBackground(iconName = "directions_car")
                CategoryIconWithBackground(iconName = "shopping_bag")
                CategoryIconWithBackground(iconName = "home")
            }
            // More expense icons
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                CategoryIconWithBackground(iconName = "sports_esports")
                CategoryIconWithBackground(iconName = "local_hospital")
                CategoryIconWithBackground(iconName = "school")
                CategoryIconWithBackground(iconName = "more_horiz")
            }
            // Income icons
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                CategoryIconWithBackground(iconName = "work")
                CategoryIconWithBackground(iconName = "trending_up")
                CategoryIconWithBackground(iconName = "card_giftcard")
                CategoryIconWithBackground(iconName = "attach_money")
            }
        }
    }
}
