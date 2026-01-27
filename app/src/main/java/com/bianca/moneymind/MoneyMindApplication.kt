package com.bianca.moneymind

import android.app.Application
import com.bianca.moneymind.domain.usecase.SeedCategoriesUseCase
import com.bianca.moneymind.domain.usecase.SeedTransactionsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class MoneyMindApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppEntryPoint {
        fun seedCategoriesUseCase(): SeedCategoriesUseCase
        fun seedTransactionsUseCase(): SeedTransactionsUseCase
    }

    override fun onCreate() {
        super.onCreate()
        seedDefaultData()
    }

    private fun seedDefaultData() {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            AppEntryPoint::class.java
        )
        applicationScope.launch {
            // 先插入分類，再插入交易
            entryPoint.seedCategoriesUseCase()()
            entryPoint.seedTransactionsUseCase()()
        }
    }
}
