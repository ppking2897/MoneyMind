package com.bianca.moneymind.domain.repository

import com.bianca.moneymind.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for app settings
 */
interface SettingsRepository {

    /**
     * Get the monthly budget setting
     */
    val monthlyBudget: Flow<Double>

    /**
     * Set the monthly budget
     */
    suspend fun setMonthlyBudget(budget: Double)

    /**
     * Get the current theme mode
     */
    val themeMode: Flow<ThemeMode>

    /**
     * Set the theme mode
     */
    suspend fun setThemeMode(mode: ThemeMode)
}
