package com.bianca.moneymind.di

import com.bianca.moneymind.BuildConfig
import com.bianca.moneymind.data.remote.gemini.GeminiService
import com.bianca.moneymind.data.remote.gemini.PromptBuilder
import com.bianca.moneymind.data.remote.ocr.MlKitOcrService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providePromptBuilder(): PromptBuilder {
        return PromptBuilder()
    }

    @Provides
    @Singleton
    fun provideGeminiService(promptBuilder: PromptBuilder): GeminiService {
        return GeminiService(promptBuilder).apply {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank()) {
                initialize(apiKey)
            }
        }
    }

    @Provides
    @Singleton
    fun provideMlKitOcrService(): MlKitOcrService {
        return MlKitOcrService()
    }
}
