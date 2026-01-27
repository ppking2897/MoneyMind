package com.bianca.moneymind.di

import com.bianca.moneymind.data.repository.AiRepositoryImpl
import com.bianca.moneymind.data.repository.CategoryRepositoryImpl
import com.bianca.moneymind.data.repository.OcrRepositoryImpl
import com.bianca.moneymind.data.repository.SettingsRepositoryImpl
import com.bianca.moneymind.data.repository.TransactionRepositoryImpl
import com.bianca.moneymind.domain.repository.AiRepository
import com.bianca.moneymind.domain.repository.CategoryRepository
import com.bianca.moneymind.domain.repository.OcrRepository
import com.bianca.moneymind.domain.repository.SettingsRepository
import com.bianca.moneymind.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        impl: AiRepositoryImpl
    ): AiRepository

    @Binds
    @Singleton
    abstract fun bindOcrRepository(
        impl: OcrRepositoryImpl
    ): OcrRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
