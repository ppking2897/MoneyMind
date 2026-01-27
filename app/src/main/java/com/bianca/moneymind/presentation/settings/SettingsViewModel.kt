package com.bianca.moneymind.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.usecase.ClearAllDataUseCase
import com.bianca.moneymind.domain.usecase.ExportTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isExporting: Boolean = false,
    val exportedCsvContent: String? = null,
    val showClearDataDialog: Boolean = false,
    val isClearing: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportTransactionsUseCase: ExportTransactionsUseCase,
    private val clearAllDataUseCase: ClearAllDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
