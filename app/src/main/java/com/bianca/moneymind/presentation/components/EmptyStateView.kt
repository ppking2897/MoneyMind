package com.bianca.moneymind.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bianca.moneymind.ui.theme.MoneyMindTheme

/**
 * A reusable empty state view component.
 *
 * @param title The main title text to display
 * @param description Optional description text below the title
 * @param icon Optional icon to display above the title
 * @param actionLabel Optional CTA button label
 * @param onActionClick Optional callback for CTA button click
 * @param modifier Modifier for the component
 */
@Composable
fun EmptyStateView(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Description
        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // CTA Button
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onActionClick) {
                Text(actionLabel)
            }
        }
    }
}

// ==================== Preview ====================

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewBasicPreview() {
    MoneyMindTheme {
        EmptyStateView(
            title = "No items found"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewWithIconPreview() {
    MoneyMindTheme {
        EmptyStateView(
            title = "No transactions yet",
            description = "Start recording your first expense!",
            icon = Icons.Default.Inbox
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewWithActionPreview() {
    MoneyMindTheme {
        EmptyStateView(
            title = "No transactions yet",
            description = "Start recording your first expense!",
            icon = Icons.Default.Inbox,
            actionLabel = "Add Transaction",
            onActionClick = {}
        )
    }
}
