package com.bianca.moneymind.domain.usecase

import com.bianca.moneymind.domain.model.InputType
import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class SeedTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke() {
        val existingTransactions = transactionRepository.getAll().first()
        if (existingTransactions.isEmpty()) {
            transactionRepository.addAll(mockTransactions)
        }
    }

    companion object {
        private val today = LocalDate.now()

        val mockTransactions = listOf(
            // 今天的交易
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 85.0,
                description = "午餐便當",
                categoryId = "expense_food",
                date = today,
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = "7-11",
                note = null,
                rawInput = null
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 35.0,
                description = "捷運",
                categoryId = "expense_transport",
                date = today,
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = null,
                note = null,
                rawInput = null
            ),
            // 昨天的交易
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 450.0,
                description = "晚餐聚餐",
                categoryId = "expense_food",
                date = today.minusDays(1),
                createdAt = Instant.now(),
                inputType = InputType.NLP,
                receiptImagePath = null,
                merchantName = "鼎泰豐",
                note = "同事聚餐",
                rawInput = "昨天晚餐鼎泰豐450"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 1200.0,
                description = "運動鞋",
                categoryId = "expense_shopping",
                date = today.minusDays(1),
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = "Nike",
                note = null,
                rawInput = null
            ),
            // 前天的交易
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 150.0,
                description = "電影票",
                categoryId = "expense_entertainment",
                date = today.minusDays(2),
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = "威秀影城",
                note = null,
                rawInput = null
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 200.0,
                description = "書籍",
                categoryId = "expense_education",
                date = today.minusDays(2),
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = "博客來",
                note = "Kotlin 學習書",
                rawInput = null
            ),
            // 本週的交易
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 3500.0,
                description = "房租水電",
                categoryId = "expense_housing",
                date = today.minusDays(3),
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = null,
                note = "本月水電費",
                rawInput = null
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 680.0,
                description = "看診掛號",
                categoryId = "expense_medical",
                date = today.minusDays(4),
                createdAt = Instant.now(),
                inputType = InputType.OCR,
                receiptImagePath = null,
                merchantName = "台大醫院",
                note = null,
                rawInput = null
            ),
            // 上週的交易
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 2800.0,
                description = "超市採購",
                categoryId = "expense_shopping",
                date = today.minusDays(8),
                createdAt = Instant.now(),
                inputType = InputType.OCR,
                receiptImagePath = null,
                merchantName = "全聯",
                note = null,
                rawInput = null
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 95.0,
                description = "早餐",
                categoryId = "expense_food",
                date = today.minusDays(9),
                createdAt = Instant.now(),
                inputType = InputType.VOICE,
                receiptImagePath = null,
                merchantName = "麥當勞",
                note = null,
                rawInput = "早餐麥當勞95元"
            ),
            // 本月的交易
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 500.0,
                description = "加油",
                categoryId = "expense_transport",
                date = today.minusDays(12),
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = "中油",
                note = null,
                rawInput = null
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                amount = 350.0,
                description = "Netflix 訂閱",
                categoryId = "expense_entertainment",
                date = today.minusDays(15),
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = "Netflix",
                note = "月費",
                rawInput = null
            ),
            // 收入
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                amount = 45000.0,
                description = "1月薪水",
                categoryId = "income_salary",
                date = today.minusDays(5),
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = null,
                note = null,
                rawInput = null
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                amount = 5000.0,
                description = "年終獎金",
                categoryId = "income_bonus",
                date = today.minusDays(10),
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = null,
                note = null,
                rawInput = null
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                amount = 1200.0,
                description = "股票股利",
                categoryId = "income_investment",
                date = today.minusDays(18),
                createdAt = Instant.now(),
                inputType = InputType.MANUAL,
                receiptImagePath = null,
                merchantName = null,
                note = "台積電",
                rawInput = null
            )
        )
    }
}
