package com.bianca.moneymind.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.TransactionType
import com.bianca.moneymind.domain.usecase.GetCategoriesUseCase
import com.bianca.moneymind.domain.usecase.GetTransactionsUseCase
import com.bianca.moneymind.presentation.analysis.components.DailyExpenseData
import com.bianca.moneymind.presentation.analysis.components.MonthlyTrendData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    companion object {
        // 設為 true 使用假資料測試，設為 false 使用真實資料
        private const val USE_MOCK_DATA = true
    }

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // 使用假資料測試
        if (USE_MOCK_DATA) {
            loadMockData()
            return
        }
        val timeRange = _uiState.value.selectedTimeRange
        val today = java.time.LocalDate.now()

        val (startDate, endDate) = when (timeRange) {
            TimeRange.MONTH -> {
                val month = _uiState.value.selectedMonth
                month.atDay(1) to month.atEndOfMonth()
            }
            TimeRange.YEAR -> {
                val year = _uiState.value.selectedYear
                java.time.LocalDate.of(year, 1, 1) to java.time.LocalDate.of(year, 12, 31)
            }
        }

        viewModelScope.launch {
            combine(
                getTransactionsUseCase.byDateRange(startDate, endDate),
                getCategoriesUseCase()
            ) { transactions, categories ->
                val categoryMap = categories.associateBy { it.id }

                val totalExpense = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                val totalIncome = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }

                // Group expenses by category
                val expensesByCategory = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .map { (categoryId, txns) ->
                        val amount = txns.sumOf { it.amount }
                        val percentage = if (totalExpense > 0) {
                            (amount / totalExpense * 100).toFloat()
                        } else 0f

                        CategoryAmount(
                            categoryId = categoryId ?: "uncategorized",
                            categoryName = categoryId?.let { categoryMap[it]?.name } ?: "未分類",
                            amount = amount,
                            percentage = percentage
                        )
                    }
                    .sortedByDescending { it.amount }

                // Group incomes by category
                val incomesByCategory = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .groupBy { it.categoryId }
                    .map { (categoryId, txns) ->
                        val amount = txns.sumOf { it.amount }
                        val percentage = if (totalIncome > 0) {
                            (amount / totalIncome * 100).toFloat()
                        } else 0f

                        CategoryAmount(
                            categoryId = categoryId ?: "uncategorized",
                            categoryName = categoryId?.let { categoryMap[it]?.name } ?: "未分類",
                            amount = amount,
                            percentage = percentage
                        )
                    }
                    .sortedByDescending { it.amount }

                // Group expenses by date for daily chart
                val dailyExpenses = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.date }
                    .map { (date, txns) ->
                        DailyExpenseData(
                            date = date,
                            amount = txns.sumOf { it.amount }
                        )
                    }
                    .sortedBy { it.date }

                // Group incomes by date for daily chart
                val dailyIncomes = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .groupBy { it.date }
                    .map { (date, txns) ->
                        DailyExpenseData(
                            date = date,
                            amount = txns.sumOf { it.amount }
                        )
                    }
                    .sortedBy { it.date }

                // Group by month for yearly trend chart
                val monthlyTrend = if (timeRange == TimeRange.YEAR) {
                    transactions
                        .groupBy { YearMonth.from(it.date) }
                        .map { (month, txns) ->
                            val expense = txns
                                .filter { it.type == TransactionType.EXPENSE }
                                .sumOf { it.amount }
                            val income = txns
                                .filter { it.type == TransactionType.INCOME }
                                .sumOf { it.amount }
                            MonthlyTrendData(
                                month = month,
                                expense = expense,
                                income = income
                            )
                        }
                        .sortedBy { it.month }
                } else {
                    emptyList()
                }

                AnalysisUiState(
                    isLoading = false,
                    selectedTimeRange = timeRange,
                    selectedMonth = _uiState.value.selectedMonth,
                    selectedYear = _uiState.value.selectedYear,
                    totalExpense = totalExpense,
                    totalIncome = totalIncome,
                    categoryExpenses = expensesByCategory,
                    categoryIncomes = incomesByCategory,
                    dailyExpenses = dailyExpenses,
                    dailyIncomes = dailyIncomes,
                    monthlyTrend = monthlyTrend
                )
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun onTimeRangeChange(timeRange: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = timeRange) }
        loadData()
    }

    fun onMonthChange(month: YearMonth) {
        _uiState.update { it.copy(selectedMonth = month) }
        loadData()
    }

    fun onYearChange(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
        loadData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 載入假資料用於測試
     */
    private fun loadMockData() {
        val timeRange = _uiState.value.selectedTimeRange
        val selectedMonth = _uiState.value.selectedMonth
        val selectedYear = _uiState.value.selectedYear

        // 根據時間範圍生成對應的假資料
        when (timeRange) {
            TimeRange.MONTH -> loadMockDataForMonth(selectedMonth)
            TimeRange.YEAR -> loadMockDataForYear(selectedYear)
        }
    }

    /**
     * 載入月視圖的假資料
     */
    private fun loadMockDataForMonth(month: YearMonth) {
        val today = java.time.LocalDate.now()
        val currentYearMonth = YearMonth.now()
        val daysInMonth = month.lengthOfMonth()

        // 判斷哪些日子有資料（過去和今天有，未來沒有）
        val lastDayWithData = when {
            month.isBefore(currentYearMonth) -> daysInMonth  // 過去月份：整月都有資料
            month == currentYearMonth -> today.dayOfMonth     // 當月：只到今天
            else -> 0                                          // 未來月份：沒有資料
        }

        // 生成該月份**所有天數**的支出資料（未來的日子金額為 0）
        val mockDailyExpenses = (1..daysInMonth).map { day ->
            val hasData = day <= lastDayWithData
            DailyExpenseData(
                date = month.atDay(day),
                amount = if (hasData) (100..800).random().toDouble() else 0.0
            )
        }

        // 該月份的收入（假設每月 5 號發薪）
        // 同樣生成所有天數，只有 5 號有收入
        val mockDailyIncomes = (1..daysInMonth).map { day ->
            val hasIncome = day == 5 && day <= lastDayWithData
            DailyExpenseData(
                date = month.atDay(day),
                amount = if (hasIncome) 45000.0 else 0.0
            )
        }

        // 計算該月總額（只計算有資料的部分）
        val totalExpense = mockDailyExpenses.filter { it.amount > 0 }.sumOf { it.amount }
        val totalIncome = mockDailyIncomes.filter { it.amount > 0 }.sumOf { it.amount }

        // 該月份的類別分佈
        val mockExpenseCategories = if (totalExpense > 0) {
            listOf(
                CategoryAmount("expense_food", "餐飲", totalExpense * 0.36, 36.0f),
                CategoryAmount("expense_transport", "交通", totalExpense * 0.26, 26.0f),
                CategoryAmount("expense_shopping", "購物", totalExpense * 0.23, 23.0f),
                CategoryAmount("expense_entertainment", "娛樂", totalExpense * 0.15, 15.0f)
            )
        } else {
            emptyList()
        }

        val mockIncomeCategories = if (totalIncome > 0) {
            listOf(CategoryAmount("income_salary", "薪資", 45000.0, 100.0f))
        } else {
            emptyList()
        }

        // 在月視圖也生成近 6 個月的月度趨勢資料（使用上面已宣告的 currentYearMonth）
        val mockMonthlyTrendForMonth = (-5..0).map { offset ->
            val trendMonth = month.plusMonths(offset.toLong())
            val hasData = !trendMonth.isAfter(currentYearMonth)
            MonthlyTrendData(
                month = trendMonth,
                expense = if (hasData) (8000..20000).random().toDouble() else 0.0,
                income = if (hasData) 45000.0 else 0.0
            )
        }

        _uiState.value = AnalysisUiState(
            isLoading = false,
            selectedTimeRange = TimeRange.MONTH,
            selectedMonth = month,
            selectedYear = _uiState.value.selectedYear,
            totalExpense = totalExpense,
            totalIncome = totalIncome,
            categoryExpenses = mockExpenseCategories,
            categoryIncomes = mockIncomeCategories,
            dailyExpenses = mockDailyExpenses,
            dailyIncomes = mockDailyIncomes,
            monthlyTrend = mockMonthlyTrendForMonth
        )
    }

    /**
     * 載入年視圖的假資料
     */
    private fun loadMockDataForYear(year: Int) {
        val currentYearMonth = YearMonth.now()
        val currentYear = currentYearMonth.year

        // 判斷哪些月份有資料
        val lastMonthWithData = when {
            year < currentYear -> 12                          // 過去年份：全年都有資料
            year == currentYear -> currentYearMonth.monthValue // 當年：只到當前月份
            else -> 0                                          // 未來年份：沒有資料
        }

        // 生成整年 12 個月的資料（未來月份金額為 0）
        val mockMonthlyTrend = (1..12).map { monthValue ->
            val month = YearMonth.of(year, monthValue)
            val hasData = monthValue <= lastMonthWithData
            MonthlyTrendData(
                month = month,
                expense = if (hasData) (10000..25000).random().toDouble() else 0.0,
                income = if (hasData) {
                    45000.0 + (if (monthValue == 12 && year < currentYear) 5000.0 else 0.0)
                } else {
                    0.0
                }
            )
        }

        // 計算年度總額
        val totalExpense = mockMonthlyTrend.sumOf { it.expense }
        val totalIncome = mockMonthlyTrend.sumOf { it.income }

        // 年度類別分佈
        val mockExpenseCategories = if (totalExpense > 0) {
            listOf(
                CategoryAmount("expense_food", "餐飲", totalExpense * 0.36, 36.0f),
                CategoryAmount("expense_transport", "交通", totalExpense * 0.26, 26.0f),
                CategoryAmount("expense_shopping", "購物", totalExpense * 0.23, 23.0f),
                CategoryAmount("expense_entertainment", "娛樂", totalExpense * 0.15, 15.0f)
            )
        } else {
            emptyList()
        }

        // 使用有資料的月份數量來計算，不是全年 12 個月
        val monthsWithData = lastMonthWithData
        val mockIncomeCategories = if (totalIncome > 0 && monthsWithData > 0) {
            val salaryTotal = 45000.0 * monthsWithData
            val bonusTotal = if (year < currentYear && monthsWithData == 12) 5000.0 else 0.0  // 只有完整年度才有年終
            val total = salaryTotal + bonusTotal
            listOf(
                CategoryAmount("income_salary", "薪資", salaryTotal, (salaryTotal / total * 100).toFloat()),
                CategoryAmount("income_bonus", "獎金", bonusTotal, (bonusTotal / total * 100).toFloat())
            ).filter { it.amount > 0 }
        } else {
            emptyList()
        }

        _uiState.value = AnalysisUiState(
            isLoading = false,
            selectedTimeRange = TimeRange.YEAR,
            selectedMonth = _uiState.value.selectedMonth,
            selectedYear = year,
            totalExpense = totalExpense,
            totalIncome = totalIncome,
            categoryExpenses = mockExpenseCategories,
            categoryIncomes = mockIncomeCategories,
            dailyExpenses = emptyList(),
            dailyIncomes = emptyList(),
            monthlyTrend = mockMonthlyTrend
        )
    }
}
