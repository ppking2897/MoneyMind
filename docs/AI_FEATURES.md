# AI 功能設計

---

## 概覽

| 功能 | 技術 | 用途 |
|------|------|------|
| NLP 自然語言解析 | Gemini API | 解析「午餐便當 85 元」→ 結構化資料 |
| OCR 收據掃描 | ML Kit + Gemini | 拍照 → 辨識文字 → 解析結構 |
| 自動分類 | 規則 + AI 混合 | 智能分類交易 |

---

## 1. NLP 自然語言解析

### 1.1 整體流程

```
用戶輸入                      AI 解析                         結構化資料
───────────────────────────────────────────────────────────────────────
"昨天全家買咖啡 50 元"  →  [Gemini API]  →  {
                                              type: EXPENSE,
                                              amount: 50,
                                              date: 2026-01-20,
                                              merchantName: "全家",
                                              category: "餐飲/飲料",
                                              description: "咖啡"
                                            }
```

### 1.2 對話式補完流程

```
用戶: "今天早餐 80、午餐便當"
           │
           ▼
┌───────────────────────────────────┐
│ Gemini 解析結果：                   │
│ [                                 │
│   { amount: 80, complete: true }, │
│   { amount: null, missing: [amount] } │
│ ]                                 │
└───────────────────────────────────┘
           │
           ▼
AI: "「午餐便當」花了多少錢？"
           │
用戶: "85"
           │
           ▼
AI: 確認記錄以下 2 筆？
    ┌────────────────────────┐
    │ 🍳 早餐    $80   今天   │
    │ 🍱 午餐    $85   今天   │
    └────────────────────────┘
    [取消]          [確認 ✓]
           │
           ▼ 確認
儲存 + "已記錄 2 筆，今日支出 $165 ✓"
```

### 1.3 完整流程圖

```
┌──────────────────────────────────────────────────────────────────┐
│                     對話式 NLP 記帳流程                            │
└──────────────────────────────────────────────────────────────────┘

  用戶輸入
      │
      ▼
┌─────────────┐    有暫存 session?     ┌─────────────────┐
│ 檢查狀態     │ ──── 是 ─────────────→ │ 填入缺少的欄位    │
└─────────────┘                        └─────────────────┘
      │ 否                                      │
      ▼                                        │
┌─────────────┐                                │
│ Gemini 解析  │                                │
└─────────────┘                                │
      │                                        │
      ▼                                        ▼
┌─────────────────────────────────────────────────────┐
│              每筆交易檢查 missingFields               │
├─────────────────────────────────────────────────────┤
│  有缺欄位 → 暫存 session，追問                        │
│  沒缺欄位 → 進入確認流程                              │
└─────────────────────────────────────────────────────┘
      │
      ▼
┌─────────────────────────────────────────────────────┐
│                   顯示摘要確認                        │
│         [取消]              [確認 ✓]                │
└─────────────────────────────────────────────────────┘
      │
      │ 確認
      ▼
┌─────────────┐
│  存入資料庫   │ → "已記錄 N 筆 ✓"
└─────────────┘
```

### 1.4 設計決策

| 決策項目 | 選擇 | 原因 |
|----------|------|------|
| 解析失敗處理 | 對話式追問補充 | 體驗更自然 |
| API 選擇 | Gemini | 免費額度大 (每日 1500 次) |
| 模糊輸入策略 | 嚴謹模式 (不確定就問) | 避免記錯帳 |
| 多筆輸入 | 支援 | 「早餐 80、午餐 120」一次解析 |
| 追問順序 | amount → category → date | 金額最重要 |
| 暫存逾時 | 5 分鐘，不通知 | 避免殭屍資料 |
| 確認機制 | 顯示摘要讓用戶確認 | 確保正確性 |

### 1.5 Prompt 設計

```text
你是一個記帳助手。解析用戶輸入，轉成結構化交易資料。

## 背景資訊
- 今天日期：{currentDate}
- 可用類別：{categoriesJson}

## 用戶輸入
「{userInput}」

## 輸出格式 (JSON)
{
  "transactions": [
    {
      "type": "EXPENSE" | "INCOME",
      "amount": number | null,
      "date": "YYYY-MM-DD",
      "merchantName": string | null,
      "categoryParent": string,
      "categorySub": string | null,
      "description": string,
      "missingFields": ["amount", "category", ...],
      "confidence": 0.0-1.0
    }
  ]
}

## 規則
1. 可能有多筆交易，全部解析
2. 金額沒有明確數字 → null，加入 missingFields
3. 日期沒提到 → 預設今天
4. "薪水"、"入帳" 等關鍵字 → type 為 INCOME
5. 回傳純 JSON，不要其他文字
```

### 1.6 追問句模板

```kotlin
val questionTemplates = mapOf(
    "amount" to "「{description}」花了多少錢？",
    "category" to "「{description}」要歸類在哪個類別？",
    "date" to "「{description}」是什麼時候的消費？"
)
```

---

## 2. OCR 收據掃描

### 2.1 處理流程

```
┌─────────────────────────────────────────────────────────────────┐
│                      OCR 處理流程                                │
└─────────────────────────────────────────────────────────────────┘

    用戶拍照
        │
        ▼
 ┌─────────────┐
 │  ML Kit OCR  │  ← 離線、免費、快速
 └─────────────┘
        │
        ▼
 ┌─────────────────────────────────┐
 │  提取的原始文字                   │
 │  "全家便利商店                    │
 │   2026/01/21 14:32              │
 │   拿鐵咖啡     45               │
 │   合計         45"              │
 └─────────────────────────────────┘
        │
        ▼
 ┌─────────────┐
 │ Gemini API  │  ← 解析結構化資料
 └─────────────┘
        │
        ▼
 顯示確認摘要 → 儲存（不保存照片）
```

### 2.2 為什麼用混合方案？

- ML Kit 離線快速，節省 Gemini 額度
- Gemini 專注在「理解」而非「識別」
- 離線時至少能提取文字，上線後再解析

### 2.3 收據解析 Prompt

```text
你是收據解析助手。根據 OCR 提取的收據文字，解析出結構化資料。

## OCR 文字
```
{ocrText}
```

## 輸出格式 (JSON)
{
  "merchant": "商家名稱",
  "date": "YYYY-MM-DD" | null,
  "items": [
    { "name": "品項", "price": number }
  ],
  "total": number,
  "confidence": 0.0-1.0
}

## 規則
1. 金額優先找「合計」「總計」「Total」
2. 日期可能是民國年（如 113/01/21），轉成西元
3. total 必須有值，否則 confidence < 0.5
4. 回傳純 JSON
```

### 2.4 設計決策

| 決策項目 | 選擇 | 原因 |
|----------|------|------|
| OCR 引擎 | ML Kit (離線) + Gemini (解析) | 離線可用 + 智能解析 |
| 多品項處理 | 合併成一筆 | 簡化 MVP |
| 照片保存 | 不保存 | 隱私 + 節省空間 |
| 拍照方式 | 內建相機 + 輔助框線 | 更好的引導 |
| 失敗處理 | 手動輸入 / 重新拍攝 | 給用戶選擇 |

### 2.5 辨識失敗處理

```
收據太模糊、皺摺、光線差時：

AI: 收據看不太清楚，只辨識到金額 $86

    [手動輸入]  [重新拍攝]
```

---

## 3. 自動分類邏輯

### 3.1 三層優先順序

```
輸入：description / merchantName
              │
              ▼
┌──────────────────────────┐
│ ① 用戶歷史習慣匹配        │  ← 最高優先 (conf: 0.95)
│    (UserCategoryRule)    │
└──────────────────────────┘
              │ 沒匹配到
              ▼
┌──────────────────────────┐
│ ② 關鍵字規則匹配          │  ← 關鍵字優先於商家 (conf: 0.9)
│    "早餐" → 餐飲/早餐     │
└──────────────────────────┘
              │ 沒匹配到
              ▼
┌──────────────────────────┐
│ ③ 商家規則匹配            │  ← (conf: 0.9)
│    "星巴克" → 餐飲/飲料   │
└──────────────────────────┘
              │ 沒匹配到
              ▼
┌──────────────────────────┐
│ ④ Gemini AI 推測         │  ← (conf: AI 給的)
└──────────────────────────┘
              │
              ▼
       conf >= 0.7?
        │ 是      │ 否
        ▼         ▼
    直接使用   詢問用戶
```

### 3.2 為什麼要三層？

| 理由 | 說明 |
|------|------|
| 成本 | 規則匹配零成本，省 API 額度 |
| 速度 | 本地規則 < 1ms，API 要 500ms+ |
| 離線 | 沒網路時規則仍可用 |
| 準確 | 確定的分類（如「星巴克」）規則更可靠 |

### 3.3 規則引擎設計

```kotlin
// 商家規則 (精確匹配)
val merchantRules = mapOf(
    "全家" to "餐飲/其他",
    "7-11" to "餐飲/其他",
    "星巴克" to "餐飲/飲料",
    "麥當勞" to "餐飲/其他",
    "台灣大車隊" to "交通/計程車",
    "中油" to "交通/加油",
    "台電" to "居住/水電",
    "Netflix" to "娛樂/訂閱服務",
    // ...
)

// 關鍵字規則 (模糊匹配)
val keywordRules = listOf(
    Rule(keywords = ["早餐"], category = "餐飲/早餐"),
    Rule(keywords = ["午餐", "便當"], category = "餐飲/午餐"),
    Rule(keywords = ["晚餐"], category = "餐飲/晚餐"),
    Rule(keywords = ["咖啡", "拿鐵", "美式"], category = "餐飲/飲料"),
    Rule(keywords = ["捷運", "公車", "悠遊卡"], category = "交通/大眾運輸"),
    Rule(keywords = ["uber", "計程車", "小黃"], category = "交通/計程車"),
    Rule(keywords = ["加油", "95", "98"], category = "交通/加油"),
    Rule(keywords = ["電影", "威秀", "國賓"], category = "娛樂/電影"),
    Rule(keywords = ["薪水", "薪資", "月薪"], category = "薪資/正職", type = INCOME),
    // ...
)
```

### 3.4 用戶修正學習

```
用戶把「健身房」從「娛樂」改成「健康」
                │
                ▼
┌─────────────────────────────────────┐
│ 自動建立 UserCategoryRule            │
│                                     │
│ {                                   │
│   keyword: "健身房",                 │
│   matchType: CONTAINS,              │
│   categoryId: "health_sports",      │
│   hitCount: 1,                      │
│   source: USER_CORRECTION           │
│ }                                   │
└─────────────────────────────────────┘
                │
                ▼
        靜默完成，不打擾用戶
        下次「健身房」自動分到「健康」
```

### 3.5 設計決策

| 決策項目 | 選擇 | 原因 |
|----------|------|------|
| Confidence 閾值 | 0.7 | 平衡準確與詢問頻率 |
| 用戶修正後 | 自動學習，不詢問 | 不打斷流程 |
| 規則衝突 | 關鍵字 > 商家 | 用戶說「早餐」就是早餐 |
| 無法判斷時 | 直接問用戶 | 不丟進「其他」類別 |

### 3.6 處理流程總結

| 情況 | 處理方式 |
|------|----------|
| 用戶歷史匹配 | 直接使用 |
| 關鍵字規則匹配 | 直接使用 |
| 商家規則匹配 | 直接使用 |
| AI 推測 conf >= 0.7 | 直接使用 |
| AI 推測 conf < 0.7 | 詢問用戶 |
| AI 也無法判斷 | 詢問用戶 |
| 用戶修正分類 | 自動學習 |

---

## 4. API 層設計

### 4.1 架構

```
ViewModel / UseCase
        │
        ▼
┌───────────────┐
│  Repository   │  ← 抽象介面（Domain 層）
└───────────────┘
        │
        ▼
┌───────────────┐
│RepositoryImpl │  ← 實作（Data 層）
└───────────────┘
        │
┌───────┴───────┐
▼               ▼
GeminiService   MlKitOcrService
```

### 4.2 結果封裝

```kotlin
sealed class AiResult<out T> {
    data class Success<T>(val data: T) : AiResult<T>()
    data class Error<Nothing>(val exception: AiException) : AiResult<Nothing>()

    fun <R> map(transform: (T) -> R): AiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

sealed class AiException : Exception() {
    object NetworkError : AiException()
    object RateLimitExceeded : AiException()
    object InvalidResponse : AiException()
    data class Unknown(override val message: String) : AiException()

    fun toUserMessage(): String = when (this) {
        NetworkError -> "網路連線失敗，請檢查網路"
        RateLimitExceeded -> "AI 服務忙碌中，請稍後再試"
        InvalidResponse -> "AI 回應格式錯誤"
        is Unknown -> "發生錯誤：$message"
    }
}
```

### 4.3 Retry 機制

```kotlin
suspend fun <T> retryOnError(
    times: Int = 2,
    block: suspend () -> AiResult<T>
): AiResult<T> {
    repeat(times) {
        when (val result = block()) {
            is AiResult.Success -> return result
            is AiResult.Error -> {
                if (result.exception == AiException.NetworkError) {
                    delay(1000)  // 等 1 秒再試
                } else {
                    return result  // 其他錯誤不重試
                }
            }
        }
    }
    return block()  // 最後一次
}
```

### 4.4 設計決策

| 決策項目 | 選擇 | 原因 |
|----------|------|------|
| API Key 管理 | BuildConfig 內建 | MVP 簡單優先 |
| Retry 機制 | 有，網路錯誤重試 2 次 | 提高穩定性 |
| Gemini Model | gemini-1.5-flash | 快速便宜 |
| 輸出格式 | 強制 JSON mode | 回傳更穩定 |
| Prompt 管理 | PromptBuilder 集中管理 | 方便維護 |
