package com.bianca.moneymind.data.repository

import com.bianca.moneymind.data.local.datastore.SettingsDataStore
import com.bianca.moneymind.domain.model.ThemeMode
import com.bianca.moneymind.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.bianca.moneymind.data.local.datastore.ThemeMode as DataThemeMode

/**
 * Implementation of SettingsRepository using DataStore
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override val monthlyBudget: Flow<Double> = settingsDataStore.monthlyBudget

    override suspend fun setMonthlyBudget(budget: Double) {
        settingsDataStore.setMonthlyBudget(budget)
    }

    override val themeMode: Flow<ThemeMode> = settingsDataStore.themeMode.map { dataThemeMode ->
        dataThemeMode.toDomain()
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        settingsDataStore.setThemeMode(mode.toData())
    }
}

/**
 * Mapper: Data ThemeMode -> Domain ThemeMode
 */
private fun DataThemeMode.toDomain(): ThemeMode = when (this) {
    DataThemeMode.SYSTEM -> ThemeMode.SYSTEM
    DataThemeMode.LIGHT -> ThemeMode.LIGHT
    DataThemeMode.DARK -> ThemeMode.DARK
}

/**
 * Mapper: Domain ThemeMode -> Data ThemeMode
 */
private fun ThemeMode.toData(): DataThemeMode = when (this) {
    ThemeMode.SYSTEM -> DataThemeMode.SYSTEM
    ThemeMode.LIGHT -> DataThemeMode.LIGHT
    ThemeMode.DARK -> DataThemeMode.DARK
}
