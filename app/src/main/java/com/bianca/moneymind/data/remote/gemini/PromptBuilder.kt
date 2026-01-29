package com.bianca.moneymind.data.remote.gemini

import com.bianca.moneymind.domain.model.Category
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds prompts for Gemini API
 */
@Singleton
class PromptBuilder @Inject constructor() {

    /**
     * Build prompt for parsing natural language input
     */
    fun buildParsePrompt(
        userInput: String,
        categories: List<Category>,
        currentDate: LocalDate = LocalDate.now()
    ): String {
        val categoriesJson = categories.joinToString(",\n    ") {
            """{"id": "${it.id}", "name": "${it.name}", "type": "${it.type.name}"}"""
        }

        return """
你是一個記帳助手。解析用戶輸入，轉成結構化交易資料。

## 背景資訊
- 今天日期：${currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}
- 可用類別：
  [
    $categoriesJson
  ]

## 用戶輸入
「$userInput」

## 輸出格式 (JSON)
回傳純 JSON，不要有其他文字或 markdown 標記：
{
  "transactions": [
    {
      "type": "EXPENSE" 或 "INCOME",
      "amount": 數字或null（如果沒提到金額）,
      "date": "YYYY-MM-DD"（沒提到就用今天）,
      "merchantName": "商家名稱" 或 null,
      "categoryId": "類別ID"（從可用類別中選擇最接近的）,
      "description": "描述",
      "missingFields": ["amount", "categoryId"] 等缺少的欄位,
      "confidence": 0.0-1.0 的信心度
    }
  ]
}

## 規則
1. 可能有多筆交易（如「早餐80、午餐85」），全部解析
2. 金額沒有明確數字 → amount 為 null，missingFields 加入 "amount"
3. categoryId 必須從可用類別中選擇最接近的；若不確定 → missingFields 加入 "categoryId" 讓用戶確認
4. 日期沒提到 → 用今天；「昨天」→ 今天減一天
5. "薪水"、"入帳"、"收入" 等關鍵字 → type 為 "INCOME"
6. 回傳純 JSON，不要有 ```json 等標記
""".trimIndent()
    }

    /**
     * Build prompt for follow-up questions
     */
    fun buildFollowUpPrompt(
        missingField: String,
        description: String
    ): String {
        return when (missingField) {
            "amount" -> "「${description}」花了多少錢？"
            "category" -> "「${description}」要歸類在哪個類別？"
            "date" -> "「${description}」是什麼時候的消費？"
            else -> "請補充「${description}」的${missingField}"
        }
    }

    /**
     * Build prompt for direct image receipt parsing (Gemini Vision)
     */
    fun buildReceiptImagePrompt(categories: List<Category>): String {
        val categoryList = categories
            .filter { it.type.name == "EXPENSE" }
            .take(8)
            .joinToString(", ") { "${it.id}:${it.name}" }

        return """
看這張收據/發票圖片，提取以下資訊：
1. 商家名稱
2. 總金額（找「合計」「總計」「Total」等）
3. 日期（民國年請轉西元年）

可用類別：$categoryList

回傳純 JSON（不要 markdown）：
{"merchantName":"商家名","totalAmount":123,"date":"2024-01-15","suggestedCategoryId":"類別ID","confidence":0.9}

如果看不清楚某欄位，該欄位填 null。
""".trimIndent()
    }

    /**
     * Build prompt for OCR receipt parsing (fallback, minimal tokens)
     */
    @Deprecated("Use buildReceiptImagePrompt for better accuracy")
    fun buildReceiptParsePrompt(
        ocrText: String,
        categories: List<Category>
    ): String {
        val categoryList = categories
            .filter { it.type.name == "EXPENSE" }
            .take(5)
            .joinToString(",") { it.id }

        return """從收據提取:商家,金額,日期。類別:$categoryList
回傳JSON:{"merchantName":"","totalAmount":0,"date":"","suggestedCategoryId":"","confidence":0.8}
$ocrText""".trimIndent()
    }
}
