package com.bianca.moneymind.presentation.settings.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bianca.moneymind.domain.usecase.ai.DefaultRules
import com.bianca.moneymind.ui.theme.MoneyMindTheme

enum class RuleTab {
    KEYWORD, MERCHANT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnedRulesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(RuleTab.KEYWORD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分類規則", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LearnedRulesContent(
            selectedTab = selectedTab,
            onTabChange = { selectedTab = it },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun LearnedRulesContent(
    selectedTab: RuleTab,
    onTabChange: (RuleTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AI 自動分類規則",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "這些規則幫助 AI 自動將交易分類到正確的類別。當交易描述或商家名稱匹配規則時，將自動套用對應的類別。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedTab == RuleTab.KEYWORD,
                onClick = { onTabChange(RuleTab.KEYWORD) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("關鍵字規則")
            }
            SegmentedButton(
                selected = selectedTab == RuleTab.MERCHANT,
                onClick = { onTabChange(RuleTab.MERCHANT) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("商家規則")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rules List
        when (selectedTab) {
            RuleTab.KEYWORD -> KeywordRulesList()
            RuleTab.MERCHANT -> MerchantRulesList()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordRulesList() {
    val groupedRules = DefaultRules.keywordRules.groupBy { it.categoryId }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(groupedRules.entries.toList()) { (categoryId, rules) ->
            val categoryName = getCategoryDisplayName(categoryId)
            val allKeywords = rules.flatMap { it.keywords }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allKeywords.forEach { keyword ->
                            AssistChip(
                                onClick = { },
                                label = { Text(keyword, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MerchantRulesList() {
    val groupedRules = DefaultRules.merchantRules.groupBy { it.categoryId }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(groupedRules.entries.toList()) { (categoryId, rules) ->
            val categoryName = getCategoryDisplayName(categoryId)
            val merchants = rules.map { it.merchantName }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        merchants.forEach { merchant ->
                            AssistChip(
                                onClick = { },
                                label = { Text(merchant, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun getCategoryDisplayName(categoryId: String): String {
    return when (categoryId) {
        "expense_food" -> "餐飲"
        "expense_transport" -> "交通"
        "expense_shopping" -> "購物"
        "expense_entertainment" -> "娛樂"
        "expense_living" -> "生活"
        "expense_medical" -> "醫療"
        "expense_education" -> "教育"
        "expense_other" -> "其他支出"
        "income_salary" -> "薪資"
        "income_bonus" -> "獎金"
        "income_investment" -> "投資"
        "income_other" -> "其他收入"
        else -> categoryId
    }
}

// ==================== Preview ====================

@Preview(showBackground = true)
@Composable
private fun LearnedRulesContentPreview() {
    MoneyMindTheme {
        LearnedRulesContent(
            selectedTab = RuleTab.KEYWORD,
            onTabChange = {}
        )
    }
}
