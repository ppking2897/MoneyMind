package com.bianca.moneymind.domain.model

/**
 * AI-related exceptions with user-friendly messages
 */
sealed class AiException(message: String) : Exception(message) {

    data object NetworkError : AiException("網路連線失敗")

    data object RateLimitExceeded : AiException("API 額度已用完")

    data object InvalidResponse : AiException("AI 回應格式錯誤")

    data object ApiKeyMissing : AiException("API Key 未設定")

    data class ParseError(val details: String) : AiException("解析失敗: $details")

    data class Unknown(override val message: String) : AiException(message)

    fun toUserMessage(): String = when (this) {
        is NetworkError -> "網路連線失敗，請檢查網路"
        is RateLimitExceeded -> "AI 服務忙碌中，請稍後再試"
        is InvalidResponse -> "AI 回應格式錯誤，請重試"
        is ApiKeyMissing -> "API Key 未設定"
        is ParseError -> "無法解析輸入內容"
        is Unknown -> message
    }
}
