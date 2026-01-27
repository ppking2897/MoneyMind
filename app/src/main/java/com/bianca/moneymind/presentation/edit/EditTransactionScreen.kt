package com.bianca.moneymind.presentation.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bianca.moneymind.domain.model.Category
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.ui.theme.MoneyMindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    onNavigateBack: () -> Unit,
    onTransactionUpdated: () -> Unit,
    onTransactionDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle saved state
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onTransactionUpdated()
        }
    }

    // Handle deleted state
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onTransactionDeleted()
        }
    }

    // Handle error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Handle not found
    LaunchedEffect(uiState.notFound) {
        if (uiState.notFound) {
            snackbarHostState.showSnackbar("找不到交易記錄")
            onNavigateBack()
        }
    }

    EditTransactionContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onTransactionTypeChange = viewModel::onTransactionTypeChange,
        onAmountChange = viewModel::onAmountChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onCategorySelect = viewModel::onCategorySelect,
        onDateChange = viewModel::onDateChange,
        onNoteChange = viewModel::onNoteChange,
        onSaveClick = viewModel::saveTransaction,
        onDeleteClick = viewModel::showDeleteDialog,
        onDeleteConfirm = viewModel::deleteTransaction,
        onDeleteDismiss = viewModel::hideDeleteDialog,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTransactionContent(
    uiState: EditTransactionUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onTransactionTypeChange: (TransactionType) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelect: (Category) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 日期選擇器狀態
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.date
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateChange(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) { Text("確認") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("編輯記錄") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "刪除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Selector
            Text("類型", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TransactionType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = uiState.transactionType == type,
                        onClick = { onTransactionTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TransactionType.entries.size
                        )
                    ) {
                        Text(if (type == TransactionType.EXPENSE) "支出" else "收入")
                    }
                }
            }

            // Amount Input
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = onAmountChange,
                label = { Text("金額") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Description Input
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("描述") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Date Picker
            OutlinedTextField(
                value = uiState.date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                onValueChange = {},
                label = { Text("日期") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "選擇日期")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Category Selector
            Text("類別", style = MaterialTheme.typography.labelLarge)
            if (uiState.categories.isEmpty()) {
                Text(
                    "尚無類別",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.categories.take(4).forEach { category ->
                        FilterChip(
                            selected = uiState.selectedCategory?.id == category.id,
                            onClick = { onCategorySelect(category) },
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            // Note Input
            OutlinedTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                label = { Text("備註（選填）") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = onSaveClick,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSaving) "儲存中..." else "儲存")
            }
        }

        // Delete Confirmation Dialog
        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = onDeleteDismiss,
                title = { Text("確認刪除") },
                text = { Text("確定要刪除這筆交易記錄嗎？此操作無法復原。") },
                confirmButton = {
                    TextButton(onClick = onDeleteConfirm) {
                        Text("刪除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDeleteDismiss) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

// ==================== Preview ====================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun EditTransactionContentPreview() {
    MoneyMindTheme {
        EditTransactionContent(
            uiState = EditTransactionUiState.mock(),
            snackbarHostState = SnackbarHostState(),
            onNavigateBack = {},
            onTransactionTypeChange = {},
            onAmountChange = {},
            onDescriptionChange = {},
            onCategorySelect = {},
            onDateChange = {},
            onNoteChange = {},
            onSaveClick = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun EditTransactionContentLoadingPreview() {
    MoneyMindTheme {
        EditTransactionContent(
            uiState = EditTransactionUiState.mockLoading(),
            snackbarHostState = SnackbarHostState(),
            onNavigateBack = {},
            onTransactionTypeChange = {},
            onAmountChange = {},
            onDescriptionChange = {},
            onCategorySelect = {},
            onDateChange = {},
            onNoteChange = {},
            onSaveClick = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {}
        )
    }
}
