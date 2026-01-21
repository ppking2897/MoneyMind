# 資料模型

---

## 核心 Entity

### Transaction (交易記錄)

```
┌─────────────────────────────────────────────────────────┐
│ Transaction                                             │
├─────────────────────────────────────────────────────────┤
│ id: String (UUID)                                       │
│ type: Enum [EXPENSE, INCOME]                            │
│ amount: Double                                          │
│ description: String                                     │
│ categoryId: String (FK → Category)                      │
│ date: LocalDate                                         │
│ createdAt: Timestamp                                    │
│ inputType: Enum [MANUAL, VOICE, OCR, NLP]              │
│ receiptImagePath: String?                               │
│ merchantName: String?                                   │
│ note: String?                                           │
│ rawInput: String? (原始輸入，用於 AI 學習)               │
└─────────────────────────────────────────────────────────┘
```

```kotlin
// domain/model/Transaction.kt
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val category: Category,
    val date: LocalDate,
    val merchantName: String?,
    val note: String?,
    val inputType: InputType,
    val rawInput: String?,
    val createdAt: Instant
)

enum class TransactionType { EXPENSE, INCOME }
enum class InputType { MANUAL, VOICE, OCR, NLP }
```

---

### Category (類別 - 支援雙層)

```
┌─────────────────────────────────────────────────────────┐
│ Category                                                │
├─────────────────────────────────────────────────────────┤
│ id: String                                              │
│ name: String                                            │
│ icon: String (emoji 或 icon name)                       │
│ color: String (hex)                                     │
│ type: Enum [EXPENSE, INCOME]                            │
│ parentId: String? (null = 父類別)                       │
│ isDefault: Boolean (系統預設 vs 用戶自訂)               │
│ sortOrder: Int                                          │
└─────────────────────────────────────────────────────────┘
```

**雙層結構說明**：
- `parentId = null` → 這是父類別（餐飲）
- `parentId = "xxx"` → 這是子類別（早餐、午餐）
- 查詢父類別：`WHERE parentId IS NULL`
- 查詢某父類別的子類別：`WHERE parentId = :parentId`

---

### UserCategoryRule (用戶學習規則)

```
┌─────────────────────────────────────────────────────────┐
│ UserCategoryRule                                        │
├─────────────────────────────────────────────────────────┤
│ id: String (UUID)                                       │
│ keyword: String (觸發詞)                                │
│ matchType: Enum [EXACT, CONTAINS, MERCHANT]             │
│ categoryId: String (FK → Category)                      │
│ hitCount: Int (使用次數，越高越優先)                     │
│ lastUsed: LocalDate                                     │
│ source: Enum [USER_CORRECTION, USER_CREATED]            │
│ createdAt: Timestamp                                    │
└─────────────────────────────────────────────────────────┘
```

**用途**：記住用戶的分類修正，下次自動套用

---

### PendingSession (對話暫存狀態)

```
┌─────────────────────────────────────────────────────────┐
│ PendingSession                                          │
├─────────────────────────────────────────────────────────┤
│ sessionId: String                                       │
│ transactionsJson: String (List<PartialTransaction>)     │
│ currentIndex: Int (正在處理第幾筆)                      │
│ currentField: String? (正在問哪個欄位)                  │
│ createdAt: Long                                         │
│ expiresAt: Long (5 分鐘後自動清除)                      │
└─────────────────────────────────────────────────────────┘
```

**用途**：對話式記帳的中間狀態，支援追問補充

---

## 設計決策

| 決策項目 | 選擇 | 原因 |
|----------|------|------|
| 類別結構 | 雙層（parentId 自關聯）| 更細緻的分類，未來可擴充 |
| 日期處理 | 只存 LocalDate | 大多數人記帳不在意幾點幾分 |
| 金額類型 | 支援收入 + 支出 | 完整的財務追蹤 |
| rawInput 欄位 | 保留 | 用於 AI 學習和 debug |

---

## 預設類別

### 支出類別

| 父類別 | icon | color | 子類別 |
|--------|------|-------|--------|
| 餐飲 | 🍔 | #FF5722 | 早餐、午餐、晚餐、飲料、零食 |
| 交通 | 🚗 | #2196F3 | 大眾運輸、計程車/Uber、加油、停車 |
| 購物 | 🛒 | #9C27B0 | 日用品、服飾、3C、其他 |
| 居住 | 🏠 | #795548 | 房租、水電、網路、家具 |
| 娛樂 | 🎮 | #E91E63 | 電影、遊戲、訂閱服務 |
| 醫療 | 💊 | #00BCD4 | 看診、藥品 |
| 教育 | 📚 | #3F51B5 | 課程、書籍 |
| 金融 | 💰 | #FFC107 | 保險、手續費 |
| 其他 | ❓ | #607D8B | — |

### 收入類別

| 父類別 | icon | color | 子類別 |
|--------|------|-------|--------|
| 薪資 | 💼 | #4CAF50 | 正職、兼職、獎金 |
| 投資 | 📈 | #8BC34A | 股息、利息 |
| 其他 | 🎁 | #CDDC39 | 禮金、退款 |

---

## Room Entity 實作

```kotlin
// data/local/database/entity/TransactionEntity.kt
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("date"),
        Index("type")
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val type: String,  // EXPENSE, INCOME
    val amount: Double,
    val description: String,
    val categoryId: String?,
    val date: Long,  // LocalDate.toEpochDay()
    val createdAt: Long,
    val inputType: String,  // MANUAL, VOICE, OCR, NLP
    val receiptImagePath: String?,
    val merchantName: String?,
    val note: String?,
    val rawInput: String?
)
```

```kotlin
// data/local/database/entity/CategoryEntity.kt
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: String,  // EXPENSE, INCOME
    val parentId: String?,
    val isDefault: Boolean,
    val sortOrder: Int
)
```

```kotlin
// data/local/database/entity/UserCategoryRuleEntity.kt
@Entity(
    tableName = "user_category_rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("keyword"), Index("categoryId")]
)
data class UserCategoryRuleEntity(
    @PrimaryKey
    val id: String,
    val keyword: String,
    val matchType: String,  // EXACT, CONTAINS, MERCHANT
    val categoryId: String,
    val hitCount: Int,
    val lastUsed: Long,
    val source: String,  // USER_CORRECTION, USER_CREATED
    val createdAt: Long
)
```

```kotlin
// data/local/database/entity/PendingSessionEntity.kt
@Entity(tableName = "pending_sessions")
data class PendingSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val transactionsJson: String,
    val currentIndex: Int,
    val currentField: String?,
    val createdAt: Long,
    val expiresAt: Long
)
```

---

## DAO 介面

```kotlin
// data/local/database/dao/TransactionDao.kt
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getByCategory(categoryId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date = :date ORDER BY createdAt DESC")
    fun getByDate(date: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)
}
```

```kotlin
// data/local/database/dao/CategoryDao.kt
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY sortOrder")
    fun getByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY sortOrder")
    fun getParentCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder")
    fun getSubCategories(parentId: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Delete
    suspend fun delete(category: CategoryEntity)
}
```

```kotlin
// data/local/database/dao/UserRuleDao.kt
@Dao
interface UserRuleDao {
    @Query("SELECT * FROM user_category_rules ORDER BY hitCount DESC")
    fun getAll(): Flow<List<UserCategoryRuleEntity>>

    @Query("SELECT * FROM user_category_rules WHERE keyword = :keyword AND matchType = :matchType LIMIT 1")
    suspend fun findByKeyword(keyword: String, matchType: String): UserCategoryRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: UserCategoryRuleEntity)

    @Query("UPDATE user_category_rules SET hitCount = hitCount + 1, lastUsed = :now WHERE id = :id")
    suspend fun incrementHitCount(id: String, now: Long)

    @Query("DELETE FROM user_category_rules WHERE id = :id")
    suspend fun delete(id: String)
}
```

```kotlin
// data/local/database/dao/PendingSessionDao.kt
@Dao
interface PendingSessionDao {
    @Query("SELECT * FROM pending_sessions WHERE expiresAt > :now LIMIT 1")
    suspend fun getActiveSession(now: Long = System.currentTimeMillis()): PendingSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: PendingSessionEntity)

    @Query("DELETE FROM pending_sessions WHERE sessionId = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM pending_sessions WHERE expiresAt <= :now")
    suspend fun deleteExpired(now: Long = System.currentTimeMillis())
}
```

---

## Repository 介面

```kotlin
// domain/repository/TransactionRepository.kt
interface TransactionRepository {
    fun getTransactions(): Flow<List<Transaction>>
    fun getTransactionsByDateRange(start: LocalDate, end: LocalDate): Flow<List<Transaction>>
    fun getTransactionsByCategory(categoryId: String): Flow<List<Transaction>>
    fun getTransactionsByDate(date: LocalDate): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun addTransaction(transaction: Transaction)
    suspend fun addTransactions(transactions: List<Transaction>)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
}
```

```kotlin
// domain/repository/CategoryRepository.kt
interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>
    fun getParentCategories(): Flow<List<Category>>
    fun getSubCategories(parentId: String): Flow<List<Category>>
    suspend fun addCategory(category: Category)
    suspend fun deleteCategory(category: Category)
}
```
