# 技術架構

---

## 整體架構

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Clean Architecture 分層                         │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                            │
│                     (UI、ViewModel、State)                           │
├─────────────────────────────────────────────────────────────────────┤
│                         Domain Layer                                 │
│              (Use Cases、Domain Models、Repository 介面)              │
├─────────────────────────────────────────────────────────────────────┤
│                          Data Layer                                  │
│         (Repository 實作、Data Source、API、Database)                 │
└─────────────────────────────────────────────────────────────────────┘

依賴方向：外層 → 內層
         UI → Domain ← Data
```

### 核心原則

1. **依賴反轉**：Domain 層不依賴任何外層
2. **Repository 介面在 Domain**：Data 層實作介面
3. **Use Case 封裝業務邏輯**：ViewModel 只負責 UI 狀態

---

## 技術棧

### Android 開發

| 項目 | 選擇 |
|------|------|
| 語言 | Kotlin |
| UI | Jetpack Compose (Material 3) |
| 架構 | MVVM + Clean Architecture |
| 本地資料庫 | Room |
| 依賴注入 | Hilt |
| 設定儲存 | Preferences DataStore |
| 導航 | Compose Navigation |

### AI/ML 整合

| 項目 | 選擇 |
|------|------|
| NLP | Google Gemini API (gemini-1.5-flash) |
| OCR | ML Kit Text Recognition (離線、中文支援) |

### 圖表

| 項目 | 選擇 |
|------|------|
| 長條圖/折線圖 | Vico |
| 圓餅圖 | 自製 Canvas |

---

## 專案結構

```
app/src/main/java/com/bianca/moneymind/
│
├── 📁 di/                      # Dependency Injection (Hilt)
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
│
├── 📁 data/                    # Data Layer
│   ├── 📁 local/
│   │   ├── 📁 database/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── 📁 dao/
│   │   │   │   ├── TransactionDao.kt
│   │   │   │   ├── CategoryDao.kt
│   │   │   │   └── UserRuleDao.kt
│   │   │   └── 📁 entity/
│   │   │       ├── TransactionEntity.kt
│   │   │       ├── CategoryEntity.kt
│   │   │       └── UserCategoryRuleEntity.kt
│   │   └── 📁 datastore/
│   │       └── SettingsDataStore.kt
│   │
│   ├── 📁 remote/
│   │   ├── 📁 gemini/
│   │   │   ├── GeminiService.kt
│   │   │   ├── PromptBuilder.kt
│   │   │   └── 📁 dto/
│   │   │       ├── ParsedTransactionDto.kt
│   │   │       └── ReceiptParseDto.kt
│   │   └── 📁 ocr/
│   │       └── MlKitOcrService.kt
│   │
│   ├── 📁 repository/
│   │   ├── TransactionRepositoryImpl.kt
│   │   ├── CategoryRepositoryImpl.kt
│   │   ├── AiRepositoryImpl.kt
│   │   └── SettingsRepositoryImpl.kt
│   │
│   └── 📁 mapper/
│       ├── TransactionMapper.kt
│       └── CategoryMapper.kt
│
├── 📁 domain/                   # Domain Layer
│   ├── 📁 model/
│   │   ├── Transaction.kt
│   │   ├── Category.kt
│   │   ├── UserCategoryRule.kt
│   │   ├── ParsedInput.kt
│   │   └── ChatMessage.kt
│   │
│   ├── 📁 repository/           # 介面定義
│   │   ├── TransactionRepository.kt
│   │   ├── CategoryRepository.kt
│   │   ├── AiRepository.kt
│   │   └── SettingsRepository.kt
│   │
│   └── 📁 usecase/
│       ├── 📁 transaction/
│       │   ├── AddTransactionUseCase.kt
│       │   ├── GetTransactionsUseCase.kt
│       │   ├── DeleteTransactionUseCase.kt
│       │   └── UpdateTransactionUseCase.kt
│       ├── 📁 ai/
│       │   ├── ParseNaturalInputUseCase.kt
│       │   ├── ParseReceiptUseCase.kt
│       │   └── AutoCategorizeUseCase.kt
│       ├── 📁 category/
│       │   ├── GetCategoriesUseCase.kt
│       │   └── ManageCategoryUseCase.kt
│       └── 📁 analytics/
│           ├── GetMonthlyStatsUseCase.kt
│           └── GetCategoryBreakdownUseCase.kt
│
├── 📁 presentation/             # Presentation Layer
│   ├── 📁 common/
│   │   ├── 📁 components/       # 共用 Composable
│   │   │   ├── TransactionCard.kt
│   │   │   ├── CategoryPicker.kt
│   │   │   ├── AmountInput.kt
│   │   │   └── LoadingIndicator.kt
│   │   └── 📁 theme/
│   │       ├── Theme.kt
│   │       ├── Color.kt
│   │       └── Type.kt
│   │
│   ├── 📁 home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── HomeUiState.kt
│   │
│   ├── 📁 chat/                 # AI 聊天記帳
│   │   ├── ChatScreen.kt
│   │   ├── ChatViewModel.kt
│   │   ├── ChatUiState.kt
│   │   └── 📁 components/
│   │       ├── ChatBubble.kt
│   │       ├── ConfirmationCard.kt
│   │       └── InputBar.kt
│   │
│   ├── 📁 camera/               # OCR 拍照
│   │   ├── CameraScreen.kt
│   │   ├── CameraViewModel.kt
│   │   └── ReceiptOverlay.kt
│   │
│   ├── 📁 manual/               # 手動輸入
│   │   ├── ManualInputScreen.kt
│   │   └── ManualInputViewModel.kt
│   │
│   ├── 📁 analysis/             # 分析頁
│   │   ├── AnalysisScreen.kt
│   │   ├── AnalysisViewModel.kt
│   │   └── 📁 components/
│   │       ├── PieChart.kt
│   │       └── StatsCard.kt
│   │
│   ├── 📁 history/              # 歷史頁
│   │   ├── HistoryScreen.kt
│   │   └── HistoryViewModel.kt
│   │
│   └── 📁 settings/             # 設定頁
│       ├── SettingsScreen.kt
│       ├── SettingsViewModel.kt
│       └── 📁 screens/
│           ├── CategoryManageScreen.kt
│           ├── LearnedRulesScreen.kt
│           └── BudgetSettingScreen.kt
│
├── 📁 navigation/
│   ├── NavGraph.kt
│   └── Screen.kt
│
└── MainApplication.kt
```

---

## 架構決策

| 問題 | 決策 | 原因 |
|------|------|------|
| 單模組 vs 多模組 | 單模組（package 分層）| MVP 階段簡單優先 |
| UI State 管理 | 單一 UiState data class | 聊天頁狀態是疊加的 |
| 對話暫存狀態 | Room 暫存表 + 5 分鐘過期 | 持久化，App 被殺掉不丟失 |
| API 錯誤處理 | 顯示錯誤，讓用戶手動輸入 | 簡單直接 |
| 設定儲存 | Preferences DataStore | 輕量、非同步、型別安全 |

---

## 依賴清單

```kotlin
// build.gradle.kts (:app)
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // ML Kit OCR
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Vico Charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // Kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

---

## DataStore vs SharedPreferences

| | SharedPreferences | DataStore |
|---|-------------------|-----------|
| 執行緒安全 | ❌ 不安全 | ✅ 安全 |
| 非同步 | ❌ 同步阻塞 | ✅ Flow/suspend |
| 型別安全 | ❌ | ✅ (Proto 版) |
| 狀態 | 已過時 | 推薦使用 |

**本專案使用**：
- Preferences DataStore：用戶設定（主題、預算、閾值）
- Room：交易記錄、類別、學習規則
