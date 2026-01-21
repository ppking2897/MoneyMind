# 實作規劃

---

## 階段總覽

```
Phase 0: 專案初始化          ░░░░░░░░░░░░░░░░░░░░  Day 1-2
Phase 1: 資料層              ████░░░░░░░░░░░░░░░░  Day 3-5
Phase 2: 基礎 UI + 手動記帳   ████████░░░░░░░░░░░░  Day 6-10
Phase 3: AI 功能整合         ████████████░░░░░░░░  Day 11-16
Phase 4: OCR 收據掃描        ████████████████░░░░  Day 17-20
Phase 5: 分析與圖表          ████████████████████  Day 21-25

─────────────────────────────────────────────────────────────────────
                              MVP 完成線 ↑
```

---

## 依賴關係

```
Phase 0 ──→ Phase 1 ──→ Phase 2 ──┬──→ Phase 3 ──→ Phase 5
專案初始化    資料層      基礎 UI   │      AI 整合      圖表
                                  │
                                  └──→ Phase 4
                                        OCR

說明：
- Phase 3 和 Phase 4 可以平行開發（都依賴 Phase 2）
- Phase 5 需要 Phase 3 的自動分類完成
```

---

## Phase 0: 專案初始化

**目標**：建立專案骨架，能跑起來

**時程**：Day 1-2

### Checklist

- [ ] 建立 Android 專案 (Kotlin + Compose)
- [ ] 設定 build.gradle 依賴
- [ ] 建立 package 結構
  - [ ] di/
  - [ ] data/local/database/
  - [ ] data/remote/
  - [ ] domain/model/
  - [ ] domain/repository/
  - [ ] domain/usecase/
  - [ ] presentation/
  - [ ] navigation/
- [ ] 設定 Hilt DI
  - [ ] @HiltAndroidApp
  - [ ] AppModule
- [ ] 設定 Material 3 Theme
  - [ ] Theme.kt
  - [ ] Color.kt
  - [ ] Type.kt
- [ ] 建立 Navigation 骨架
  - [ ] Screen.kt
  - [ ] NavGraph.kt
- [ ] 建立空的 Screen placeholder
  - [ ] HomeScreen
  - [ ] AnalysisScreen
  - [ ] HistoryScreen
  - [ ] SettingsScreen
- [ ] 建立 MainScreen + BottomNavBar
- [ ] 設定 Gemini API Key (local.properties)

### 驗收標準

- [ ] App 能啟動
- [ ] 底部導航可切換 4 個空頁面
- [ ] FAB 選單可以彈出

---

## Phase 1: 資料層

**目標**：Room 資料庫 + Repository 完成

**時程**：Day 3-5

### Checklist

- [ ] **Database**
  - [ ] AppDatabase.kt
  - [ ] DatabaseModule.kt (Hilt)

- [ ] **Entity**
  - [ ] TransactionEntity.kt
  - [ ] CategoryEntity.kt
  - [ ] UserCategoryRuleEntity.kt
  - [ ] PendingSessionEntity.kt

- [ ] **DAO**
  - [ ] TransactionDao.kt
  - [ ] CategoryDao.kt
  - [ ] UserRuleDao.kt
  - [ ] PendingSessionDao.kt

- [ ] **Domain Model**
  - [ ] Transaction.kt
  - [ ] Category.kt
  - [ ] TransactionType.kt
  - [ ] InputType.kt
  - [ ] UserCategoryRule.kt

- [ ] **Mapper**
  - [ ] TransactionMapper.kt
  - [ ] CategoryMapper.kt

- [ ] **Repository 介面** (domain/)
  - [ ] TransactionRepository.kt
  - [ ] CategoryRepository.kt

- [ ] **Repository 實作** (data/)
  - [ ] TransactionRepositoryImpl.kt
  - [ ] CategoryRepositoryImpl.kt
  - [ ] RepositoryModule.kt (Hilt)

- [ ] **DataStore**
  - [ ] SettingsDataStore.kt

- [ ] **預設類別資料**
  - [ ] DefaultCategories.kt
  - [ ] 首次啟動時 seed data

### 驗收標準

- [ ] 可以 CRUD 交易記錄
- [ ] 預設類別已載入（支出 9 類、收入 3 類）
- [ ] Repository 單元測試通過

---

## Phase 2: 基礎 UI + 手動記帳

**目標**：能用手動方式記帳，看到記錄

**時程**：Day 6-10

### Checklist

- [ ] **共用元件** (presentation/common/components/)
  - [ ] TransactionCard.kt
  - [ ] CategoryIcon.kt
  - [ ] CategoryPicker.kt
  - [ ] AmountInput.kt
  - [ ] DatePicker.kt
  - [ ] LoadingIndicator.kt

- [ ] **首頁** (presentation/home/)
  - [ ] HomeScreen.kt
  - [ ] HomeViewModel.kt
  - [ ] HomeUiState.kt
  - [ ] DailySummary.kt
  - [ ] BudgetProgressBar.kt

- [ ] **手動輸入** (presentation/manual/)
  - [ ] ManualInputScreen.kt
  - [ ] ManualInputViewModel.kt

- [ ] **歷史** (presentation/history/)
  - [ ] HistoryScreen.kt
  - [ ] HistoryViewModel.kt
  - [ ] 日期分組列表
  - [ ] 篩選（全部/支出/收入）

- [ ] **編輯交易** (presentation/edit/)
  - [ ] EditTransactionScreen.kt
  - [ ] EditTransactionViewModel.kt
  - [ ] 刪除功能

- [ ] **Use Case**
  - [ ] AddTransactionUseCase.kt
  - [ ] GetTransactionsUseCase.kt
  - [ ] UpdateTransactionUseCase.kt
  - [ ] DeleteTransactionUseCase.kt
  - [ ] GetCategoriesUseCase.kt

### 驗收標準

- [ ] 可以手動新增支出/收入
- [ ] 首頁顯示今日交易列表
- [ ] 歷史頁可查看所有交易（依日期分組）
- [ ] 可以編輯、刪除交易
- [ ] 預算進度條顯示正確

**此時 App 已經「能用」**

---

## Phase 3: AI 功能整合

**目標**：NLP 自然語言記帳 + 自動分類

**時程**：Day 11-16

### Checklist

- [ ] **Gemini Service** (data/remote/gemini/)
  - [ ] GeminiService.kt
  - [ ] PromptBuilder.kt
  - [ ] ParsedTransactionDto.kt
  - [ ] NetworkModule.kt (Hilt)

- [ ] **AI Repository**
  - [ ] AiRepository.kt (介面)
  - [ ] AiRepositoryImpl.kt (實作)

- [ ] **結果封裝**
  - [ ] AiResult.kt
  - [ ] AiException.kt

- [ ] **Use Case** (domain/usecase/ai/)
  - [ ] ParseNaturalInputUseCase.kt
  - [ ] AutoCategorizeUseCase.kt

- [ ] **自動分類邏輯**
  - [ ] CategoryMatcher.kt (規則引擎)
  - [ ] DefaultRules.kt (商家/關鍵字規則)

- [ ] **用戶學習機制**
  - [ ] 用戶修正時建立 UserCategoryRule
  - [ ] LearnCategoryUseCase.kt

- [ ] **聊天頁面** (presentation/chat/)
  - [ ] ChatScreen.kt
  - [ ] ChatViewModel.kt
  - [ ] ChatUiState.kt
  - [ ] ChatBubble.kt
  - [ ] ConfirmationCard.kt
  - [ ] InputBar.kt
  - [ ] FollowUpQuestion.kt

- [ ] **對話狀態管理**
  - [ ] PendingSession 暫存/讀取
  - [ ] 5 分鐘逾時清除

- [ ] **語音輸入**
  - [ ] SpeechRecognizer 整合
  - [ ] 麥克風權限

- [ ] **Retry 機制**
  - [ ] 網路錯誤重試 2 次

### 驗收標準

- [ ] 可以用「午餐便當 85」自動解析
- [ ] 缺少欄位時會追問
- [ ] 確認卡片顯示，用戶確認後儲存
- [ ] 自動分類準確度 > 80%
- [ ] 用戶修正後會學習
- [ ] 語音輸入可用

**此時 App 展現 AI 價值**

---

## Phase 4: OCR 收據掃描

**目標**：拍照掃描收據

**時程**：Day 17-20

### Checklist

- [ ] **ML Kit OCR** (data/remote/ocr/)
  - [ ] MlKitOcrService.kt
  - [ ] OcrResult.kt

- [ ] **收據解析**
  - [ ] ReceiptParseDto.kt
  - [ ] 收據 Prompt 模板
  - [ ] ParseReceiptUseCase.kt

- [ ] **相機頁面** (presentation/camera/)
  - [ ] CameraScreen.kt
  - [ ] CameraViewModel.kt
  - [ ] CameraPreview.kt (CameraX)
  - [ ] ReceiptOverlay.kt (輔助框線)
  - [ ] CaptureButton.kt
  - [ ] ScanResultCard.kt

- [ ] **權限處理**
  - [ ] 相機權限請求
  - [ ] 權限被拒絕的提示

- [ ] **失敗處理**
  - [ ] 辨識失敗 UI
  - [ ] 重新拍攝 / 手動輸入 選項

### 驗收標準

- [ ] 可以拍照
- [ ] 輔助框線正確顯示
- [ ] ML Kit 辨識文字成功
- [ ] Gemini 解析收據結構
- [ ] 顯示確認卡片
- [ ] 儲存交易（不保存照片）

---

## Phase 5: 分析與圖表

**目標**：資料視覺化

**時程**：Day 21-25

### Checklist

- [ ] **Use Case** (domain/usecase/analytics/)
  - [ ] GetMonthlyStatsUseCase.kt
  - [ ] GetCategoryBreakdownUseCase.kt
  - [ ] GetDailyExpensesUseCase.kt

- [ ] **分析頁面** (presentation/analysis/)
  - [ ] AnalysisScreen.kt
  - [ ] AnalysisViewModel.kt
  - [ ] AnalysisUiState.kt

- [ ] **圖表元件** (presentation/analysis/components/)
  - [ ] PieChart.kt (自製 Canvas)
  - [ ] 動畫效果
  - [ ] 點擊互動
  - [ ] DailyExpenseChart.kt (Vico)
  - [ ] MonthlyTrendChart.kt (Vico)
  - [ ] TimeRangeSelector.kt
  - [ ] SummaryCard.kt
  - [ ] CategoryRankList.kt

- [ ] **類別交易列表** (presentation/category/)
  - [ ] CategoryTransactionsScreen.kt
  - [ ] CategoryTransactionsViewModel.kt

- [ ] **圖表互動**
  - [ ] 圓餅圖點擊 → 跳轉類別交易列表
  - [ ] 時間範圍切換（日/週/月/年）

### 驗收標準

- [ ] 圓餅圖顯示類別分佈
- [ ] 長條圖顯示每日支出
- [ ] 可切換時間範圍
- [ ] 點擊圓餅圖跳轉正確
- [ ] 動畫流暢

**MVP 完成！**

---

## Phase 6: 設定與收尾 (MVP 後)

**目標**：完善設定功能、UI 打磨

### Checklist

- [ ] **設定頁面** (presentation/settings/)
  - [ ] SettingsScreen.kt
  - [ ] SettingsViewModel.kt

- [ ] **設定子頁面**
  - [ ] ManageCategoriesScreen.kt (管理類別)
  - [ ] LearnedRulesScreen.kt (管理學習規則)
  - [ ] BudgetSettingScreen.kt (預算設定)
  - [ ] ThemeSettingScreen.kt (主題設定)
  - [ ] AboutScreen.kt (關於)

- [ ] **主題切換**
  - [ ] 跟隨系統 / 淺色 / 深色
  - [ ] 動態色彩 (Material You)

- [ ] **資料匯出**
  - [ ] CSV 匯出
  - [ ] 分享功能

- [ ] **UI 打磨**
  - [ ] 載入狀態
  - [ ] 空狀態
  - [ ] 錯誤狀態
  - [ ] 動畫轉場

---

## 驗收 Checklist 總結

### Phase 2 結束時 (Day 10)

- [ ] 能手動新增支出/收入
- [ ] 首頁顯示今日交易
- [ ] 歷史頁能查看所有交易
- [ ] 能編輯、刪除交易

**此時 App 已經「能用」**

### Phase 3 結束時 (Day 16)

- [ ] 能用自然語言記帳
- [ ] AI 會追問缺少的資訊
- [ ] 自動分類準確
- [ ] 用戶修正會學習

**此時 App 展現 AI 價值**

### Phase 5 結束時 (Day 25)

- [ ] 圓餅圖、長條圖正常顯示
- [ ] 可切換時間範圍
- [ ] 點擊圖表有互動

**MVP 完成！**

---

## 風險與備案

| 風險 | 影響 | 備案 |
|------|------|------|
| Gemini API 不穩定 | AI 功能不可用 | 降級到手動輸入 |
| OCR 準確度低 | 收據掃描體驗差 | 提供手動修正介面 |
| 圖表 Library 不符需求 | 開發時間增加 | 改用 MPAndroidChart |
| 分類準確度不夠 | 用戶體驗差 | 增加規則、降低信心閾值 |
