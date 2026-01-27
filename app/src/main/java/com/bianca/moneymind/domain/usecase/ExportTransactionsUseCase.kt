package com.bianca.moneymind.domain.usecase

import com.bianca.moneymind.domain.model.Transaction
import com.bianca.moneymind.domain.repository.CategoryRepository
import com.bianca.moneymind.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExportTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(): String {
        val transactions = transactionRepository.getAll().first()
        val categories = categoryRepository.getAll().first()
        val categoryMap = categories.associateBy { it.id }

        val header = "日期,類型,金額,類別,描述,商家,備註"
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val rows = transactions.map { transaction ->
            val date = transaction.date.format(dateFormatter)
            val type = when (transaction.type) {
                com.bianca.moneymind.domain.model.TransactionType.EXPENSE -> "支出"
                com.bianca.moneymind.domain.model.TransactionType.INCOME -> "收入"
            }
            val amount = transaction.amount.toString()
            val category = transaction.categoryId?.let { categoryMap[it]?.name } ?: ""
            val description = escapeCsvField(transaction.description)
            val merchant = escapeCsvField(transaction.merchantName ?: "")
            val note = escapeCsvField(transaction.note ?: "")

            "$date,$type,$amount,$category,$description,$merchant,$note"
        }

        return (listOf(header) + rows).joinToString("\n")
    }

    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
}
