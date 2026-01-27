package com.bianca.moneymind.presentation.settings.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bianca.moneymind.ui.theme.MoneyMindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSettingScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetSettingViewModel = hiltViewModel()
) {
    val currentBudget by viewModel.currentBudget.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("每月預算", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        BudgetSettingContent(
            currentBudget = currentBudget,
            onSaveBudget = { budget ->
                viewModel.saveBudget(budget)
                onNavigateBack()
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun BudgetSettingContent(
    currentBudget: Double,
    onSaveBudget: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var budgetText by remember(currentBudget) {
        mutableStateOf(currentBudget.toLong().toString())
    }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    text = "設定每月預算",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "預算可以幫助你追蹤支出，當支出接近或超過預算時，首頁會顯示警告。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = budgetText,
            onValueChange = { value ->
                budgetText = value.filter { it.isDigit() }
                isError = budgetText.isEmpty() || budgetText.toLongOrNull() == null
            },
            label = { Text("每月預算金額") },
            prefix = { Text("$") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isError,
            supportingText = if (isError) {
                { Text("請輸入有效的金額") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        // Quick select buttons
        Text(
            text = "快速選擇",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(10000, 20000, 30000, 50000).forEach { amount ->
                Button(
                    onClick = {
                        budgetText = amount.toString()
                        isError = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("$${amount / 1000}K")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val budget = budgetText.toDoubleOrNull()
                if (budget != null && budget > 0) {
                    onSaveBudget(budget)
                }
            },
            enabled = !isError && budgetText.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("儲存")
        }
    }
}

// ==================== Preview ====================

@Preview(showBackground = true)
@Composable
private fun BudgetSettingContentPreview() {
    MoneyMindTheme {
        BudgetSettingContent(
            currentBudget = 20000.0,
            onSaveBudget = {}
        )
    }
}
