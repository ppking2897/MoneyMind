package com.bianca.moneymind.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.model.ThemeMode
import com.bianca.moneymind.domain.repository.SettingsRepository
import com.bianca.moneymind.domain.usecase.ClearAllDataUseCase
import com.bianca.moneymind.domain.usecase.ExportTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val isExporting: Boolean = false,
    val exportedCsvContent: String? = null,
    val showClearDataDialog: Boolean = false,
    val isClearing: Boolean = false,
    val message: String? = null,
    val currentBudgetDisplay: String = "$20,000",
    val currentThemeLabel: String = "跟隨系統"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportTransactionsUseCase: ExportTransactionsUseCase,
    private val clearAllDataUseCase: ClearAllDataUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.monthlyBudget.collect { budget ->
                val formatted = NumberFormat.getNumberInstance(Locale.getDefault())
                    .format(budget.toLong())
                _uiState.update { it.copy(currentBudgetDisplay = "$$formatted") }
            }
        }
        viewModelScope.launch {
            settingsRepository.themeMode.collect { mode ->
                val label = when (mode) {
                    ThemeMode.SYSTEM -> "跟隨系統"
                    ThemeMode.LIGHT -> "淺色模式"
                    ThemeMode.DARK -> "深色模式"
                }
                _uiState.update { it.copy(currentThemeLabel = label) }
            }
        }
    }

    fun exportToCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val csvContent = exportTransactionsUseCase()
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportedCsvContent = csvContent
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        message = "匯出失敗: ${e.message}"
                    )
                }
            }
        }
    }

    fun onExportHandled() {
        _uiState.update { it.copy(exportedCsvContent = null) }
    }

    fun showClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = true) }
    }

    fun dismissClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = false) }
    }

    fun confirmClearData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearing = true, showClearDataDialog = false) }
            try {
                clearAllDataUseCase()
                _uiState.update {
                    it.copy(
                        isClearing = false,
                        message = "已清除所有資料"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isClearing = false,
                        message = "清除失敗: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
