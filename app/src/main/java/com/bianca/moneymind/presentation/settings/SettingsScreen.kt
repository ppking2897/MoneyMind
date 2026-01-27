package com.bianca.moneymind.presentation.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bianca.moneymind.ui.theme.MoneyMindTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Create file launcher for saving CSV
    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            uiState.exportedCsvContent?.let { csvContent ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
            }
            viewModel.onExportHandled()
        }
    }

    // Handle export when CSV content is ready
    LaunchedEffect(uiState.exportedCsvContent) {
        uiState.exportedCsvContent?.let {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            createFileLauncher.launch("MoneyMind_$timestamp.csv")
        }
    }

    // Show snackbar messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Clear data confirmation dialog
    if (uiState.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearDataDialog,
            title = { Text("確認清除") },
            text = { Text("確定要刪除所有交易記錄嗎？此操作無法復原。") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmClearData
                ) {
                    Text("確認刪除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissClearDataDialog) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        SettingsContent(
            currentBudgetDisplay = uiState.currentBudgetDisplay,
            currentThemeLabel = uiState.currentThemeLabel,
            onNavigateToCategories = onNavigateToCategories,
            onNavigateToRules = onNavigateToRules,
            onNavigateToBudget = onNavigateToBudget,
            onNavigateToTheme = onNavigateToTheme,
            onNavigateToAbout = onNavigateToAbout,
            onExportCsv = viewModel::exportToCsv,
            onClearData = viewModel::showClearDataDialog,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun SettingsContent(
    currentBudgetDisplay: String,
    currentThemeLabel: String,
    onNavigateToCategories: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onExportCsv: () -> Unit,
    onClearData: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 外觀
        item {
            SettingsSection(title = "外觀") {
                SettingsItem(
                    icon = Icons.Default.ColorLens,
                    title = "主題模式",
                    subtitle = currentThemeLabel,
                    onClick = onNavigateToTheme
                )
            }
        }

        // 類別管理
        item {
            SettingsSection(title = "類別管理") {
                SettingsItem(
                    icon = Icons.Default.Category,
                    title = "管理類別",
                    subtitle = "新增、編輯或刪除類別",
                    onClick = onNavigateToCategories
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.Psychology,
                    title = "管理學習規則",
                    subtitle = "查看 AI 學習的分類規則",
                    onClick = onNavigateToRules
                )
            }
        }

        // 預算
        item {
            SettingsSection(title = "預算") {
                SettingsItem(
                    icon = Icons.Default.Savings,
                    title = "每月預算",
                    subtitle = currentBudgetDisplay,
                    onClick = onNavigateToBudget
                )
            }
        }

        // 資料
        item {
            SettingsSection(title = "資料") {
                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    title = "匯出資料 (CSV)",
                    subtitle = "將交易記錄匯出為 CSV 檔案",
                    onClick = onExportCsv
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "清除所有資料",
                    subtitle = "刪除所有交易記錄",
                    onClick = onClearData,
                    isDestructive = true
                )
            }
        }

        // 關於
        item {
            SettingsSection(title = "關於") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "關於此 App",
                    subtitle = "版本 1.0.0",
                    onClick = onNavigateToAbout
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
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== Preview ====================

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    MoneyMindTheme {
        SettingsContent(
            currentBudgetDisplay = "$20,000",
            currentThemeLabel = "跟隨系統",
            onNavigateToCategories = {},
            onNavigateToRules = {},
            onNavigateToBudget = {},
            onNavigateToTheme = {},
            onNavigateToAbout = {},
            onExportCsv = {},
            onClearData = {}
        )
    }
}
