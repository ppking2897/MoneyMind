package com.bianca.moneymind.presentation.settings.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bianca.moneymind.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetSettingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val currentBudget: StateFlow<Double> = settingsRepository.monthlyBudget
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 20000.0
        )

    fun saveBudget(budget: Double) {
        viewModelScope.launch {
            settingsRepository.setMonthlyBudget(budget)
        }
    }
}
